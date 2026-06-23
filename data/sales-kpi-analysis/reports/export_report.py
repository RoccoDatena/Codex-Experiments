import csv
from collections import defaultdict
import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages

csv_path = r"C:\Users\RD\Documents\Codex\data\sales-kpi-analysis\data\sales_data.csv"

orders = set()
customers = set()

sum_net_rev = 0.0
sum_profit = 0.0
sum_margin = 0.0
n_rows = 0

prod_rev = defaultdict(float)
region_profit = defaultdict(float)
month_rev = defaultdict(float)
disc_margin = defaultdict(list)

with open(csv_path, newline='', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    for row in reader:
        n_rows += 1
        orders.add(row['order_id'])
        customers.add(row['customer_id'])
        net_rev = float(row['net_revenue'])
        profit = float(row['profit'])
        margin = float(row['margin_pct'])
        prod = row['product']
        region = row['region']
        month = row['order_month']
        disc = float(row['discount_pct'])

        sum_net_rev += net_rev
        sum_profit += profit
        sum_margin += margin

        prod_rev[prod] += net_rev
        region_profit[region] += profit
        month_rev[month] += net_rev
        disc_margin[disc].append(margin)

kpis = {
    'Total Orders': len(orders),
    'Total Customers': len(customers),
    'Total Net Revenue': sum_net_rev,
    'Total Profit': sum_profit,
    'Avg Order Value (AOV)': sum_net_rev / n_rows if n_rows else 0,
    'Avg Margin %': sum_margin / n_rows if n_rows else 0,
}

# Insights
prod_sorted = sorted(prod_rev.items(), key=lambda x: x[1], reverse=True)
top3_rev = sum(v for _, v in prod_sorted[:3])
share_top3 = (top3_rev / sum_net_rev * 100) if sum_net_rev else 0
best_region = sorted(region_profit.items(), key=lambda x: x[1], reverse=True)[0]
peak_months = sorted(month_rev.items(), key=lambda x: x[1], reverse=True)[:2]
low_months = sorted(month_rev.items(), key=lambda x: x[1])[:2]
avg_margin_by_disc = sorted(((d, sum(vals)/len(vals)) for d, vals in disc_margin.items()), key=lambda x: x[0])

# Simple PDF report
out_pdf = r"C:\Users\RD\Documents\Codex\data\sales-kpi-analysis\reports\executive_summary.pdf"
with PdfPages(out_pdf) as pdf:
    fig = plt.figure(figsize=(8.27, 11.69))  # A4 portrait
    fig.text(0.08, 0.94, "Executive Summary", fontsize=18, weight='bold')
    fig.text(0.08, 0.90, "Project: Sales KPI Analysis", fontsize=12)

    y = 0.84
    fig.text(0.08, y, "Highlights", fontsize=14, weight='bold'); y -= 0.03
    fig.text(0.10, y, f"- Top 3 products generate ~{share_top3:.1f}% of net revenue.", fontsize=11); y -= 0.025
    fig.text(0.10, y, f"- Highest profit region: {best_region[0]}.", fontsize=11); y -= 0.025
    fig.text(0.10, y, f"- Revenue peaks: {peak_months[0][0]}, {peak_months[1][0]}.", fontsize=11); y -= 0.025
    fig.text(0.10, y, f"- Lowest months: {low_months[0][0]}, {low_months[1][0]}.", fontsize=11); y -= 0.025
    fig.text(0.10, y, f"- Margin drops with discount (0%: {avg_margin_by_disc[0][1]:.2f}% -> 20%: {avg_margin_by_disc[-1][1]:.2f}%).", fontsize=11); y -= 0.05

    fig.text(0.08, y, "KPI Snapshot", fontsize=14, weight='bold'); y -= 0.03
    for k, v in kpis.items():
        fig.text(0.10, y, f"{k}: {v:,.2f}" if isinstance(v, float) else f"{k}: {v}", fontsize=11)
        y -= 0.022

    fig.text(0.08, y-0.02, "Recommendations", fontsize=14, weight='bold')
    y -= 0.05
    fig.text(0.10, y, "- Invest in top products and best-performing regions.", fontsize=11); y -= 0.025
    fig.text(0.10, y, "- Review discount policy to protect margins.", fontsize=11)

    plt.axis('off')
    pdf.savefig(fig)
    plt.close(fig)

print("PDF report saved to", out_pdf)
