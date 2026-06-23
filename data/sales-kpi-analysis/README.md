# Sales KPI Analysis (Business)

Mini-progetto data-analysis con dataset **fittizio realistico** per analizzare vendite, margini e trend mensili.

## Obiettivo
Fornire una vista chiara e “business-friendly” delle performance di vendita:
- ricavi e profitti complessivi
- andamento mensile
- prodotti e categorie più importanti
- confronto tra regioni e canali
- effetto degli sconti sulla marginalità

## Struttura
- `data/sales_data.csv` dataset sintetico (2000 ordini, 2024-2025)
- `data/generate_data.py` script per rigenerare il dataset
- `notebooks/sales_kpi_analysis.ipynb` notebook principale
- `reports/` spazio per eventuali esportazioni

## Come eseguire (locale)
1. Crea un ambiente Python (opzionale ma consigliato)
2. Installa i requisiti
3. Apri il notebook ed esegui le celle

Comandi suggeriti:
```bash
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
jupyter notebook
```

## KPI principali calcolati
- Total Orders
- Total Customers
- Total Net Revenue
- Total Profit
- Avg Order Value (AOV)
- Avg Margin %

## Note
Il dataset è sintetico ma costruito con logiche realistiche (prezzi, costi, sconti, canali, regioni). È perfetto per un portfolio data-analyst orientato al business.
