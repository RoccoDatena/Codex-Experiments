from __future__ import annotations

import json
import os
import sys
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import parse_qs, urlparse
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError

from matplotlib.path import Path as MplPath
import numpy as np
import xarray as xr


BASE_DIR = Path(__file__).resolve().parent
STATIC_DIR = BASE_DIR / "static"
CACHE_DIR = BASE_DIR / "cache"
CACHE_DIR.mkdir(exist_ok=True)
PROVINCES_GEOJSON_PATH = STATIC_DIR / "basilicata-provinces.geojson"
COMUNI_JSON_PATH = STATIC_DIR / "comuni_basilicata.json"

HOST = "127.0.0.1"
PORT = 8000

DATASET_NAME = "europe-extreme-precipitation-risk-indicators"
PRODUCT_NAME = "yearly"
SOURCE = "e-obs"
OUTPUT_FORMAT = "netcdf"
DEFAULT_START_YEAR = 2000
DEFAULT_END_YEAR = 2019
REQUEST_TIMEOUT_SECONDS = 600
STATUS_POLL_INTERVAL_SECONDS = 2
VARIABLE_OPTIONS = {
    "rr1": "Numero di giorni di pioggia",
    "cwd": "Giorni consecutivi di pioggia",
    "rr20mm": "Numero di eventi superiori a 20 mm",
}

# Approximate rectangular bounding box that contains Basilicata.
# DDS accepts a rectangle, not the exact administrative border.
BASILICATA_BBOX = {
    "north": 41.05,
    "south": 39.90,
    "east": 16.90,
    "west": 15.45,
}

def normalize_variables(selected_variables: list[str] | None) -> list[str]:
    if not selected_variables:
        return ["rr1"]
    normalized = []
    for variable in selected_variables:
        key = variable.strip().lower()
        if key not in VARIABLE_OPTIONS:
            raise RuntimeError(f"Variabile non supportata: {variable}")
        normalized.append(key)
    return list(dict.fromkeys(normalized))


def load_comuni() -> list[dict]:
    if not COMUNI_JSON_PATH.exists():
        return []
    return json.loads(COMUNI_JSON_PATH.read_text(encoding="utf-8"))


def cache_path_for_request(years: list[str], variables: list[str]) -> Path:
    years_part = f"{years[0]}-{years[-1]}" if len(years) > 1 else years[0]
    variables_part = "-".join(variables)
    return CACHE_DIR / f"{DATASET_NAME}-{PRODUCT_NAME}-{variables_part}-basilicata-{years_part}.nc"


def build_years(start_year: int, end_year: int) -> list[str]:
    if start_year > end_year:
        raise RuntimeError("L'anno iniziale deve essere minore o uguale all'anno finale.")
    if start_year < 1950 or end_year > 2100:
        raise RuntimeError("Intervallo anni non valido.")
    return [str(year) for year in range(start_year, end_year + 1)]


def build_request_payload(years: list[str], variables: list[str]) -> dict:
    return {
        "area": BASILICATA_BBOX,
        "variable": variables,
        "time": {"year": years},
        "source": SOURCE,
        "format": OUTPUT_FORMAT,
    }


def retrieve_dataset(years: list[str], variables: list[str]) -> Path:
    config = load_dds_config()
    download_path = cache_path_for_request(years, variables)
    request_id = submit_dds_request(config, build_request_payload(years, variables))
    wait_for_dds_completion(config, request_id)
    download_dds_result(config, request_id, download_path)
    if not download_path.exists():
        raise RuntimeError("Il download DDS non ha prodotto il file NetCDF atteso.")
    return download_path


def load_dds_config() -> dict:
    env_url = os.environ.get("DDSAPI_URL")
    env_key = os.environ.get("DDSAPI_KEY")
    rc_path = Path(os.environ.get("DDSAPI_RC", Path.home() / ".ddsapirc"))

    file_config: dict[str, str] = {}
    if rc_path.exists():
        for raw_line in rc_path.read_text(encoding="utf-8").splitlines():
            if ":" not in raw_line:
                continue
            key, value = raw_line.split(":", 1)
            file_config[key.strip()] = value.strip()

    base_url = env_url or file_config.get("url") or "https://ddshub.cmcc.it/api/v2"
    api_key = env_key or file_config.get("key")
    if not api_key:
        raise RuntimeError(
            "Manca la API key DDS. Imposta DDSAPI_KEY oppure crea ~/.ddsapirc con url e key."
        )
    return {"url": base_url.rstrip("/"), "key": api_key}


def dds_headers(config: dict) -> dict:
    return {
        "User-Token": config["key"],
        "Content-Type": "application/json",
        "Accept": "application/json",
    }


def dds_request_json(method: str, url: str, headers: dict, payload: dict | None = None):
    data = None if payload is None else json.dumps(payload).encode("utf-8")
    request = Request(url, data=data, headers=headers, method=method)
    try:
        with urlopen(request, timeout=REQUEST_TIMEOUT_SECONDS) as response:
            charset = response.headers.get_content_charset() or "utf-8"
            body = response.read().decode(charset)
            if not body:
                return None
            return json.loads(body)
    except HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"DDS API error {exc.code}: {detail}") from exc
    except URLError as exc:
        raise RuntimeError(f"Errore di connessione verso DDS: {exc.reason}") from exc


def submit_dds_request(config: dict, payload: dict) -> int:
    url = f"{config['url']}/datasets/{DATASET_NAME}/{PRODUCT_NAME}/execute"
    response = dds_request_json("POST", url, dds_headers(config), payload)
    if not isinstance(response, int):
        raise RuntimeError(f"Risposta inattesa da DDS durante submit: {response}")
    return response


def wait_for_dds_completion(config: dict, request_id: int) -> None:
    import time

    url = f"{config['url']}/requests/{request_id}/status"
    started_at = time.time()
    last_status = None
    for _ in range(max(1, REQUEST_TIMEOUT_SECONDS // STATUS_POLL_INTERVAL_SECONDS)):
        response = dds_request_json("GET", url, dds_headers(config))
        if not isinstance(response, dict) or "status" not in response:
            raise RuntimeError(f"Risposta inattesa da DDS durante polling: {response}")
        status = response["status"]
        if status != last_status:
            elapsed = round(time.time() - started_at, 1)
            print(f"DDS request {request_id} status: {status} after {elapsed}s")
            last_status = status
        if status == "DONE":
            return
        if status == "FAILED":
            fail_reason = response.get("fail_reason", "motivo non disponibile")
            raise RuntimeError(f"Richiesta DDS fallita: {fail_reason}")
        if status not in {"PENDING", "RUNNING"}:
            raise RuntimeError(f"Stato DDS non gestito: {status}")
        time.sleep(STATUS_POLL_INTERVAL_SECONDS)
    raise RuntimeError(
        f"Timeout in attesa della conclusione della richiesta DDS dopo {REQUEST_TIMEOUT_SECONDS} secondi."
    )


def download_dds_result(config: dict, request_id: int, target_path: Path) -> None:
    size_url = f"{config['url']}/requests/{request_id}/size"
    size_response = dds_request_json("GET", size_url, dds_headers(config))
    if not isinstance(size_response, int):
        raise RuntimeError(f"Risposta inattesa da DDS sulla dimensione file: {size_response}")

    download_url = f"{config['url']}/download/{request_id}"
    request = Request(download_url, headers={"User-Token": config["key"]}, method="GET")
    try:
        with urlopen(request, timeout=REQUEST_TIMEOUT_SECONDS) as response:
            content_type = response.headers.get("Content-Type", "")
            if "application/zip" in content_type:
                raise RuntimeError(
                    "DDS ha restituito un archivio zip. Questa demo si aspetta un singolo file NetCDF."
                )
            data = response.read()
    except HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"Errore download DDS {exc.code}: {detail}") from exc
    except URLError as exc:
        raise RuntimeError(f"Errore di connessione durante il download DDS: {exc.reason}") from exc

    if len(data) != size_response:
        raise RuntimeError(
            f"Download incompleto da DDS: attesi {size_response} byte, ricevuti {len(data)}."
        )

    target_path.write_bytes(data)


def detect_coordinate_name(dataset: xr.Dataset, names: tuple[str, ...]) -> str:
    for name in names:
        if name in dataset.coords or name in dataset.dims:
            return name
    raise RuntimeError(f"Coordinate non trovate. Attese una di: {', '.join(names)}")


def normalize_year_label(raw_value) -> str:
    if hasattr(raw_value, "dt"):
        year = int(raw_value.dt.year)
        return str(year)
    if np.issubdtype(type(raw_value), np.datetime64):
        return str(np.datetime_as_string(raw_value, unit="Y"))[:4]
    text = str(raw_value)
    return text[:4]


def build_lon_lat_mesh(dataset: xr.Dataset, lon_name: str, lat_name: str) -> tuple[np.ndarray, np.ndarray]:
    lon_values = np.asarray(dataset[lon_name].values)
    lat_values = np.asarray(dataset[lat_name].values)
    if lon_values.ndim == 1 and lat_values.ndim == 1:
        lon_grid, lat_grid = np.meshgrid(lon_values, lat_values)
        return lon_grid, lat_grid
    return lon_values, lat_values


def province_feature_name(feature: dict) -> str:
    return feature.get("properties", {}).get("prov_name", "Provincia")


def iter_polygon_rings(geometry: dict):
    geom_type = geometry.get("type")
    coordinates = geometry.get("coordinates", [])
    if geom_type == "Polygon":
        for polygon in [coordinates]:
            yield polygon
    elif geom_type == "MultiPolygon":
        for polygon in coordinates:
            yield polygon
    else:
        raise RuntimeError(f"Geometria non supportata per la mappa: {geom_type}")


def points_inside_polygon(points: np.ndarray, ring: list) -> np.ndarray:
    path = MplPath(np.asarray(ring))
    return path.contains_points(points)


def build_province_mask(feature: dict, lon_grid: np.ndarray, lat_grid: np.ndarray) -> np.ndarray:
    points = np.column_stack([lon_grid.ravel(), lat_grid.ravel()])
    mask = np.zeros(points.shape[0], dtype=bool)
    for polygon in iter_polygon_rings(feature.get("geometry", {})):
        if not polygon:
            continue
        outer_ring = polygon[0]
        polygon_mask = points_inside_polygon(points, outer_ring)
        for hole in polygon[1:]:
            polygon_mask &= ~points_inside_polygon(points, hole)
        mask |= polygon_mask
    return mask.reshape(lon_grid.shape)


def load_province_features() -> list[dict]:
    data = json.loads(PROVINCES_GEOJSON_PATH.read_text(encoding="utf-8"))
    return data.get("features", [])


def compute_province_series(
    dataset: xr.Dataset,
    variable_name: str,
    lat_name: str,
    lon_name: str,
    time_name: str,
) -> dict[str, list[float | None]]:
    lon_grid, lat_grid = build_lon_lat_mesh(dataset, lon_name, lat_name)
    data_array = dataset[variable_name]
    province_series: dict[str, list[float | None]] = {}
    for feature in load_province_features():
        province_name = province_feature_name(feature)
        mask_2d = build_province_mask(feature, lon_grid, lat_grid)
        mask = xr.DataArray(
            mask_2d,
            coords={lat_name: dataset[lat_name].values, lon_name: dataset[lon_name].values},
            dims=(lat_name, lon_name),
        )
        masked = data_array.where(mask)
        provincial_mean = masked.mean(dim=[lat_name, lon_name], skipna=True)
        
        val = provincial_mean.values
        if np.isscalar(val) or val.ndim == 0:
             province_series[province_name] = [None if np.isnan(val) else round(float(val), 2)]
        else:
             province_series[province_name] = [
                None if np.isnan(value) else round(float(value), 2)
                for value in val
             ]
    return province_series


def load_series(
    start_year: int,
    end_year: int,
    variables: list[str] | None = None,
    force_refresh: bool = False,
    location_name: str | None = None,
) -> dict:
    years = build_years(start_year, end_year)
    requested_variables = normalize_variables(variables)
    download_path = cache_path_for_request(years, requested_variables)
    if force_refresh or not download_path.exists():
        retrieve_dataset(years, requested_variables)

    with xr.open_dataset(download_path) as dataset:
        lat_name = detect_coordinate_name(dataset, ("lat", "latitude", "y"))
        lon_name = detect_coordinate_name(dataset, ("lon", "longitude", "x"))
        time_name = detect_coordinate_name(dataset, ("time", "year"))
        series: dict[str, dict] = {}
        province_series: dict[str, dict] = {}
        
        target_location = None
        target_province = None
        
        if location_name == "prov-potenza":
            target_province = "Potenza"
        elif location_name == "prov-matera":
            target_province = "Matera"
        elif location_name:
            comuni = load_comuni()
            target_location = next((c for c in comuni if c["nome"].lower() == location_name.lower()), None)

        for variable_name in requested_variables:
            if variable_name not in dataset.data_vars:
                available = ", ".join(dataset.data_vars.keys())
                raise RuntimeError(
                    f"La variabile '{variable_name}' non e' presente nel file NetCDF. Disponibili: {available}"
                )
            
            data_array = dataset[variable_name]
            
            if target_province:
                # Media provinciale (usando maschera GeoJSON)
                lon_grid, lat_grid = build_lon_lat_mesh(dataset, lon_name, lat_name)
                features = load_province_features()
                feature = next((f for f in features if province_feature_name(f) == target_province), None)
                if not feature:
                    raise RuntimeError(f"Geometria non trovata per la provincia: {target_province}")
                
                mask_2d = build_province_mask(feature, lon_grid, lat_grid)
                mask = xr.DataArray(
                    mask_2d,
                    coords={lat_name: dataset[lat_name].values, lon_name: dataset[lon_name].values},
                    dims=(lat_name, lon_name),
                )
                masked = data_array.where(mask)
                values_source = masked.mean(dim=[lat_name, lon_name], skipna=True)
                location_label = f"Provincia di {target_province}"
            elif target_location:
                # Estrazione puntuale per il comune
                point_data = data_array.sel(
                    {lat_name: target_location["lat"], lon_name: target_location["lon"]}, 
                    method="nearest"
                )
                values_source = point_data
                location_label = f"Comune di {target_location['nome']}"
            else:
                # Media regionale
                values_source = data_array.mean(dim=[lat_name, lon_name], skipna=True)
                location_label = "Media Regionale (Basilicata)"

            if time_name not in values_source.dims:
                raise RuntimeError("La serie temporale attesa non contiene una dimensione tempo.")

            years = [normalize_year_label(value) for value in values_source[time_name].values]
            values = [
                None if np.isnan(value) else round(float(value), 2)
                for value in values_source.values
            ]
            series[variable_name] = {
                "label": f"{VARIABLE_OPTIONS.get(variable_name, variable_name)} - {location_label}",
                "values": values,
            }
            
            if not target_location:
                province_series[variable_name] = {
                    "label": VARIABLE_OPTIONS.get(variable_name, variable_name),
                    "provinces": compute_province_series(
                        dataset=dataset,
                        variable_name=variable_name,
                        lat_name=lat_name,
                        lon_name=lon_name,
                        time_name=time_name,
                    ),
                }

    return {
        "dataset": DATASET_NAME,
        "product": PRODUCT_NAME,
        "variables": requested_variables,
        "variable_labels": {key: VARIABLE_OPTIONS[key] for key in requested_variables},
        "source": SOURCE,
        "bbox": BASILICATA_BBOX,
        "location": target_location,
        "province": target_province,
        "request_years": years,
        "years": years,
        "series": series,
        "province_series": province_series,
        "cache_file": str(download_path),
        "note": (
            f"Dati estratti per {location_label}. "
            "La media regionale e' calcolata sul rettangolo Basilicata."
        ),
    }


class RequestHandler(BaseHTTPRequestHandler):
    def do_GET(self) -> None:  # noqa: N802
        parsed = urlparse(self.path)
        if parsed.path == "/":
            self.serve_file(STATIC_DIR / "index.html", "text/html; charset=utf-8")
            return
        if parsed.path.startswith("/static/"):
            relative_path = parsed.path.removeprefix("/static/")
            file_path = (STATIC_DIR / relative_path).resolve()
            try:
                file_path.relative_to(STATIC_DIR.resolve())
            except ValueError:
                self.send_error(HTTPStatus.NOT_FOUND, "File non trovato.")
                return
            content_type = "application/octet-stream"
            if file_path.suffix == ".geojson":
                content_type = "application/geo+json; charset=utf-8"
            elif file_path.suffix == ".html":
                content_type = "text/html; charset=utf-8"
            elif file_path.suffix == ".js":
                content_type = "application/javascript; charset=utf-8"
            elif file_path.suffix == ".css":
                content_type = "text/css; charset=utf-8"
            elif file_path.suffix == ".json":
                content_type = "application/json; charset=utf-8"
            self.serve_file(file_path, content_type)
            return
        if parsed.path == "/api/series":
            query = parse_qs(parsed.query)
            force_refresh = query.get("refresh", ["0"])[0] == "1"
            try:
                start_year = int(query.get("start", [str(DEFAULT_START_YEAR)])[0])
                end_year = int(query.get("end", [str(DEFAULT_END_YEAR)])[0])
            except ValueError:
                self.send_json(
                    HTTPStatus.BAD_REQUEST,
                    {"error": "I parametri start ed end devono essere numeri interi."},
                )
                return
            variables = query.get("variable")
            location = query.get("location", [None])[0]
            self.serve_series(start_year, end_year, variables, force_refresh, location)
            return
        self.send_error(HTTPStatus.NOT_FOUND, "Risorsa non trovata.")

    def log_message(self, format: str, *args) -> None:  # noqa: A003
        sys.stdout.write("%s - - [%s] %s\n" % (self.address_string(), self.log_date_time_string(), format % args))

    def serve_file(self, file_path: Path, content_type: str) -> None:
        if not file_path.exists():
            self.send_error(HTTPStatus.NOT_FOUND, "File non trovato.")
            return
        content = file_path.read_bytes()
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(content)))
        self.end_headers()
        self.wfile.write(content)

    def serve_series(
        self,
        start_year: int,
        end_year: int,
        variables: list[str] | None,
        force_refresh: bool,
        location: str | None = None,
    ) -> None:
        try:
            payload = load_series(
                start_year=start_year,
                end_year=end_year,
                variables=variables,
                force_refresh=force_refresh,
                location_name=location,
            )
            self.send_json(HTTPStatus.OK, payload)
        except Exception as exc:  # pragma: no cover - runtime path
            try:
                request_payload = build_request_payload(
                    build_years(start_year, end_year),
                    normalize_variables(variables),
                )
            except Exception:
                request_payload = None
            self.send_json(
                HTTPStatus.INTERNAL_SERVER_ERROR,
                {
                    "error": str(exc),
                    "hint": (
                        "Controlla di avere una API key DDS disponibile tramite "
                        "DDSAPI_KEY oppure nel file ~/.ddsapirc."
                    ),
                    "request_payload": request_payload,
                },
            )


    def send_json(self, status: HTTPStatus, payload: dict) -> None:
        body = json.dumps(payload, ensure_ascii=True).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


def main() -> None:
    server = ThreadingHTTPServer((HOST, PORT), RequestHandler)
    print(f"Server avviato su http://{HOST}:{PORT}")
    server.serve_forever()


if __name__ == "__main__":
    main()
