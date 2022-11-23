---
subtitle: Repair
---
# Repair

Repairs the schema history table.

![Repair](assets/command-repair.png)

Repair is your tool to fix issues with the schema history table. It has a few main uses:
- Remove failed migration entries (only for databases that do NOT support DDL transactions)
- Realign the checksums, descriptions, and types of the applied migrations with the ones of the available migrations
- Mark all missing migrations as **deleted**
    - As a result, `repair` must be given the same [`locations`](Configuration/parameters/locations) as `migrate`!

## Usage
{% include commandUsage.html command="Repair" %}
