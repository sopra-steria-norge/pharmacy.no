
## Overordnet systemskisse

![Systemkart](system.png)


## Miljøbehov over tid

|              | Dev         | System      | Kjede       | eHelse      | Stress     | Prod        |
|--------------|-------------|-------------|-------------|-------------|------------|-------------|
| *Sensitivt?* | Nei         | Nei         | Nei         | ?           | Nei        | Ja          |
| *Oppetid*    | Utvikler PC | Ad hoc      | 95% 9-16    | 95% 9-16    | Under test | 99.97% 8-20 |
| *Kopier*     | Utvikler PC | 1-3         | 5-10        | 1           | 2 (3?)     | 2 (3?)      |
| *Last 2018Q3*| 10 req/min  | 100 req/min | 100 req/min | 100 req/min | 1000 req/s | 1000 req/s  |
| *Last 2018Q1*| 10 req/min  | 100 req/min | 100 req/min | 100 req/min | 1000 req/s | 10   req/s  |
| *Last 2017*  | 10 req/min  | 100 req/min |             |             | 10 req/s   |             |

## Miljøer

![Produksjon](system-prod.png)

![Stress test](system-stress.png)

![Kjede integrasjonstest](system-kjedetest.png)

![E-Helse akseptansetest](system-ehelsetest.png)

![Akseptansetest](system-acceptance.png)

![Systemtest](system-systemtest.png)
