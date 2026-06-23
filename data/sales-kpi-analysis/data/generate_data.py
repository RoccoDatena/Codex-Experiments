import csv
import random
from datetime import datetime, timedelta

random.seed(42)

n_orders = 2000
start_date = datetime(2024, 1, 1)
end_date = datetime(2025, 12, 31)

products = [
    ("Laptop Pro 14", "Electronics"),
    ("Laptop Air 13", "Electronics"),
    ("Monitor 27", "Electronics"),
    ("Smartphone X", "Electronics"),
    ("Wireless Headphones", "Electronics"),
    ("Office Chair", "Furniture"),
    ("Standing Desk", "Furniture"),
    ("Desk Lamp", "Furniture"),
    ("Notebook Pack", "Stationery"),
    ("Pen Set", "Stationery"),
    ("Water Bottle", "Accessories"),
    ("Backpack", "Accessories"),
]
regions = ["North", "Center", "South", "Islands"]
channels = ["Online", "Retail", "Wholesale"]

price_ranges = {
    "Electronics": (80, 1500),
    "Furniture": (30, 600),
    "Stationery": (5, 40),
    "Accessories": (10, 120),
}

order_ids = [f"ORD{100000+i}" for i in range(n_orders)]

rows = []
for i in range(n_orders):
    oid = order_ids[i]
    odt = start_date + timedelta(days=random.randint(0, (end_date-start_date).days))
    cid = f"CUST{1000+random.randint(0, 499)}"
    prod, cat = random.choice(products)
    qty = random.choices([1, 2, 3, 4, 5], weights=[35, 25, 20, 12, 8], k=1)[0]
    pmin, pmax = price_ranges[cat]
    unit_price = round(random.uniform(pmin, pmax), 2)
    unit_cost = round(unit_price * random.uniform(0.55, 0.80), 2)
    discount_pct = round(random.choices([0, 5, 10, 15, 20], weights=[45, 20, 18, 12, 5], k=1)[0], 2)
    region = random.choices(regions, weights=[35, 30, 25, 10], k=1)[0]
    channel = random.choices(channels, weights=[55, 30, 15], k=1)[0]

    gross_revenue = qty * unit_price
    discount_amount = gross_revenue * (discount_pct / 100.0)
    net_revenue = gross_revenue - discount_amount
    total_cost = qty * unit_cost
    profit = net_revenue - total_cost
    margin_pct = (profit / net_revenue) * 100 if net_revenue else 0
    order_month = odt.strftime("%Y-%m")

    rows.append({
        "order_id": oid,
        "order_date": odt.strftime("%Y-%m-%d"),
        "customer_id": cid,
        "product": prod,
        "category": cat,
        "region": region,
        "channel": channel,
        "quantity": qty,
        "unit_price": unit_price,
        "unit_cost": unit_cost,
        "discount_pct": discount_pct,
        "gross_revenue": round(gross_revenue, 2),
        "discount_amount": round(discount_amount, 2),
        "net_revenue": round(net_revenue, 2),
        "total_cost": round(total_cost, 2),
        "profit": round(profit, 2),
        "margin_pct": round(margin_pct, 2),
        "order_month": order_month,
    })

with open("sales_data.csv", "w", newline="", encoding="utf-8") as f:
    writer = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
    writer.writeheader()
    writer.writerows(rows)

print("Created sales_data.csv with", len(rows), "rows")
