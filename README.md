# ComEdBillParser

This program parses ComEd files and splits out the interval data based on customer account/premise/meter map

## Prerequisites

The following dependencies are necessary to run:

* Gson-2.6.2
* Apache Commons Email 1.5
* Apache Commons Lang 3.7
* Apache PI 3.17
* JavaxMail

Email server configuration settings will need to be set depending how your organization is set up.

## Running

The program can output results to a CSV file or XLSX file by changing the syntax

CSV
```
fileName += ".csv";
...
new CSVWriter(fileLocation, columns, val);
```

Excel
```
fileName += ".xlsx";
...
new ExcelWriter(fileLocation, columns, val);
```

## Customer Map

Customer map is a JSON file with the following structure:

```
Array (
    Object {
        name: String
        folder: String
        map: Array(
            Object {
                account: String,
                premise: String,
                meters: Array
            }
        )
    }
)
```

