# Frontend locale CMCC DDS

Mini app locale per mostrare in una pagina HTML i giorni di pioggia annuali in Basilicata
dal dataset DDS `europe-extreme-precipitation-risk-indicators`.

## Cosa fa

- usa chiamate HTTP dirette verso DDS lato server, senza esporre la API key nel browser
- scarica il NetCDF con la variabile `rr1`
- calcola la media spaziale sul bounding box della Basilicata
- mostra un grafico annuale 2000-2019

## Limite importante

La DDS API che stiamo usando accetta un'`area` rettangolare con:

- `north`
- `south`
- `east`
- `west`

Quindi il filtro geografico non corrisponde al confine esatto della Regione Basilicata,
ma a un rettangolo che la contiene.

## Setup

1. Attiva l'ambiente virtuale:

   ```powershell
   .\.venv\Scripts\Activate.ps1
   ```

2. Crea il file `C:\Users\<tuo_utente>\.ddsapirc` con:

   ```yaml
   url: https://ddshub.cmcc.it/api/v2
   key: YOUR_API_KEY
   ```

   In alternativa puoi impostare la variabile d'ambiente `DDSAPI_KEY`.

3. Avvia il server:

   ```powershell
   .\.venv\Scripts\python.exe .\app.py
   ```

4. Apri nel browser:

   [http://127.0.0.1:8000](http://127.0.0.1:8000)

## Coordinate usate

Bounding box approssimativo della Basilicata:

- north: `41.05`
- south: `39.90`
- east: `16.90`
- west: `15.45`
