# Kie server estension to retrieve tasks and process variables

New APIs:

**GET** `server/queries/variables`

- Query parameters: `vars` a list of variables to enquery

**POST** `server/queries/variables`

- Query parameters: `vars` a list of variables to enquery
- Payload: `org.kie.server.api.model.definition.QueryFilterSpec`

    example:

    ```json
    {
        "order-by" : null,
        "order-asc" : false,
        "query-params" : [ {
            "cond-column" : "processinstanceid",
            "cond-operator" : "GREATER_THAN",
            "cond-values" : [ 9 ]
        } ],
        "result-column-mapping" : null,
        "order-by-clause" : null
    }
    ```

In order to filter on a variable, that variable must be retrieved (defined in `vars` query parameter)

**WARNING** Process Variable are prefixed with `VAR_` to avoid confusion with the task informations.

## Examples

Retrieve a task where the process variable `lastname` is `bond`:

```sh
curl -u user:password -X POST "http://localhost:8080/kie-server/services/rest/server/queries/variables?vars=name&vars=lastname" -H "accept: application/json" -H "content-type: application/json" -d "{ \"order-by\" : null, \"order-asc\" : false, \"query-params\" : [ { \"cond-column\" : \"var_lastname\", \"cond-operator\" : \"EQUALS_TO\", \"cond-values\" : [ \"bond\" ] } ], \"result-column-mapping\" : null, \"order-by-clause\" : null}"
```