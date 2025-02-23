---
subtitle: Redgate Compare Filter Format
---

## Format

Redgate Compare filter files have the .rgf extension.
Under the hood they are structured as JSON.

### Filtering objects example

```json
{
  "version": "1.1",
  "postFilters":
  [
    {
      "filterBy": "objectType",
      "filterValue": "table",
      "effect": "exclude"
    }
  ]
}
```

The above example filter can be read as "exclude all objects of type (`objectType`) table".

### Filtering properties example

```json
{
  "version": "1.1",
  "postFilters":
  [
    {
      "filterTarget": "property",
      "filterBy": "name",
      "filterValue": "collations",
      "effect": "exclude"
    }
  ]
}
```

The above example filter can be read as "Exclude all properties with the name collations".

## Syntax

Currently only post-filters (which apply at comparison/deployment time) are supported. In the future this file may also
include pre-filters which apply when querying the database.

The list of filters is applied, in order, for each object in the comparison. The resulting include/exclude state after
all the filters have been processed is then used for that object. This means that more general filter rules can appear
at the top of the list, and more specific exceptions can appear at the bottom.

### `filterBy` - (mandatory)

| Property value | Description                                                                              |
|----------------|------------------------------------------------------------------------------------------|
| `any`          | Matches any object.                                                                      |
| `objectType`   | Matches items that are objects e.g table, function, stored procedure etc.                |
| `name`         | Currently only properties can be filtered by name. So `filterTarget` has to be property. |

### `filterTarget` - (Optional) - Defaults to object

| Property value | Description                                              |
|----------------|----------------------------------------------------------|
| `object`       | Will cause `filterBy` to affect objects.                 |
| `property`     | Will cause `filterBy` to affect properties of an object. |

### `filterValue` - (mandatory)

This is the value you wish to match and want the filter to act on.

**Note: An invalid combination of `filterBy` and `filterTarget` will cause an error.**