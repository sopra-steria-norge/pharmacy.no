    
# Innledning

Løsningsbeskrivelsen av DIFA er delt inn i fire områder:

1. Omfanget av løsningen. Dette gir en overordnet beskrivelse av hva som inngår i løsningen. Som et vedlegg til kapittelet har leverandøren inkludert et utkast til en komplett product backlog med estimater.
2. Prosjektgjennomføring. Dette gir en beskrivelse av metodene som benyttes, med leveranser og aktiviteter; organisering med teammedlemmer og tidsperspektiv. Beskivelsen dekker spesielt sikkerhet og testing.
3. Systemdesign beskriver hvordan løsningen fungerer og kommuniserer med omverden. Beskrivelsen omfatter mange, men ikke alle de funksjonelle aspektene. Beskrivelsen er strukturert etter 4C modellen: Context, Container, Component og Classes.
4. Administrative detaljer. Vurderinger som ikke er omfattet av andre deler av løsningsforslaget beskrives her.

## Executive Summary

Leverandørens forståelse av oppgaven, er at DIFA skal være et system som håndterer resepter, refusjoner, farmasøytiske tjenester og pasientjournal for disse tjenestene, og er tenkt å være et system som systemene til kjedene skal integreres mot. Kjedene vil selv utvikle sine logistikksystemer og kassasystemer (Point-Of-Sale eller POS), og vil gjøre kall mot DIFAs tjenester for å håndtere det faglige arbeidet i et apotek. Dokumentet beskriver også en opsjon på et brukergrensesnitt for de apoteker som ønsker å håndtere resepter gjennom den bakenforliggende løsningen DIFA.

Gjennom dette dokumentet beskriver leverandøren hvordan vi forstår prosessene som ligger i et slikt system, samt fremheve hvilke tjenester vi forstår at dette systemet skal levere. Dokumentet beskriver først funksjonaliteten i systemet i form av prosessbeskrivelse, deretter hvordan denne disse prosessene vil bli implementert i faser og med en prosjektgjennomføringsmodell. Til slutt beskriver dokumentet illustrerende deler av løsningen for å vise hvordan helheten henger sammen og hvordan noen kritiske enkeltkomponenter fungerer i detalj.

Leverandøren vil spesielt presisere at vi legger opp til å levere veldig tidlig en teknisk spesifikasjon og kjørende test-versjon av API som gjør det mulig for kjedene å starte sine etableringsprosjekter i 2017.

DIFA vil levere følgende tjenester:

| Område                | Tjeneste                                  |
|-----------------------|-------------------------------------------|
| Reseptur              | Finn person                               |
|                       | List resepter for person eller referanse  |
|                       | Ekspeder resepter                         |
|                       | Velg legemiddel/vare på resept            |
|                       | Beregne refusjon                          |
|                       | Oppdater reseptetikett                    |
|                       | Teknisk kontroll                          |
|                       | Farmasøytkontroll                         |
|                       | Klargjør utlevering                       |
|                       | Utlevering fullført                       |
| Refusjonskrav         | Vis refusjonskravstatus                   |
| Tjeneste i apotek     | Start tjeneste                            |
|                       | Oppdater tjenestejournal                  |
|                       | Fullfør tjeneste                          |
| Pasientjournal        | Vis reseptutleveringer for person         |
|                       | Vis farmasøytiske tjenester for person    |
|                       | Vis innsynslogg for person                |
|                       | Vis logg over intervensjoner, varsler, interaksjoner, kansellerte ekspedisjoner |
| Rapportering          | Hent rapport til offentlig instans        |
|                       | Hent datagrunnlag til bransjestatistikk   |

Merk at alle oppslag på pasientinformasjon vil logges med årsak til oppslaget og eventuell legitimasjon for den som krevde oppslaget. Merk at all rapporteringsinformasjon vil ha fjernet koblinger til identifiserbare personer.


## Resultatmål

Avtalen dekker leveranse av første versjon av DIFA som vil inneholde nødvendig funksjonalitet for å understøtte reseptbehandling i apoteker. Systemet DIFA vil i versjon 1.0 levere et API til apotekkjedene for håndtering av resepter, refusjon og utvalgte farmasøytiske tjenester. Leveransen innebærer tilstrekkelig ytelseskrav levert til et definert nettverksmessig point-of-delivery. Leveransen inneholder nødvendige test- og driftsmekanismer for å sikre at apotekkjedene kan ta i bruk tjenesten samt integrasjon med tjenester på Norsk helsenett som skal til for å realisere tjenesten.

Overgangen fra FarmaPro til DIFA vil frigjøre kjedene til å kontrollere sine egne IT-system porteføljer etter egne behov og prioriteringer. Ved å slippe FarmaPro lisense vil kjede få frigjort midler til å kjøpe, tilpasse og drifte egne løsninger.

Leverandøren er forberedt på å påta seg et betydelig ansvar for at lover og forskrifter vil følges i den nye løsningen. Selv om apotekbransjen og farmasøytene i apotek har pasientens behov som sitt viktigste kompetansefelt, vil *kjedes IT-satsninger* kunne ønske å fokusere på de kommersielle aspektene av virksomheten med vissheten om at DIFA vil garantere at pasientens rettigheter og behov er ivaretatt.


## Endringer i denne versjonen av løsningsbeskrivelsen

Leverandøren har mottatt en rekke innspill som fortsatt ikke er innarbeidet i denne versjonen av systemdesignet. Vi legger allikevel ved et uferdig systemdesign for å gi forståelse av strukturen på løsningen. Strukturen på dokumentet forventes ikke å undergå betydlige endringer fram til prøvetilbudet.


# Funksjonelt omfang

Leverandøren har valgt å gruppere funksjonaliteten i grovkornede funksjonelle områder som tilsvarer det vi ser som sammenhengende funksjonalitet. Hvert område er tekstlig beskrevet med en funksjonell flyt. Hvert sted i denne funksjonelle flyten vil typisk gi opphav til en eller flere product backlog items. Dette kapittelet utgjør en uttømmende beskrivelse av prosjektets omfang, men detaljer under hvert punkt er utelatt i beskrivelsen. Teksten skal *definere* omfanget, men beskrivelsen av detaljene forutsetter at prosjektmedlemmene i felleskap med funksjonelle eksperter diskuterer og fastsetter forløpende i prosjektgjennomføringen.

Detaljer som ikke er beskrevet rundt hvordan forretningsregler er implementert og meldinger er utfylt vil som en hovedregel bruke FarmaPro som kilde til hvordan de fungerer. e-Resept og reseptur har mange spesialregler og det er utenfor omfanget av løsningsbeskrivelsen å detaljere alle, men leverandøren vurderer FarmaPro som et godt svar på spørsmål om spesifikke forretningsregler.

Omfang som ikke inngår i et funksjonelt område er beskrevet som Krav til tjenesten DIFA. Dette inngår også i product backlog.

Et utkast til en fullstendig product backlog ligger vedlagt.

## Begrepsmodell

Begrepsmodellen viser sammenhengen mellom de ordene som brukes for å beskrive funksjonaliteten i DIFA. Formålet med figuren er å gi en oversikt over forretningsverden som DIFA lever i. De sentrale begrepene i modellen er:

* *Pasient* representerer den som skal benytte resepten. Pasient kan være en faktisk pasient, lege for bruk i egen praksis eller en dyreeier i forbindelse med en veterinærresept. (Figuren har utelatt denne kompleksiteten for å unngå visuell kompleksitet)
* *Resept* beskriver en pasients autorisasjon for å motta ett enkelt legemiddel og eventuelt få dekket prisen for denne
* Vi har introdusert begrepet *ReseptBunke* for å svare opp en uklarhet i lovverket. Dersom en helseperson forskriver flere legemidler til en pasient på samme visitt (teknisk sett: på samme dag) så kan dette forståes som én resept eller flere resepter. I tolkningen av blåreseptforskriftens egenandelstak kan én resept inneholde flere legemidler, mens i grensesnittet mot Reseptformidleren og i FHIR sitt begrepsapparat vil hvert legemiddel være en separat resept. Leverandøren benytter den siste terminologien og bruker begrepet "ReseptBunke" for å beskrive listen av resepter (legemidler) som var forskrevet på samme visitt.
* *ReseptUtlevering* dokumenterer hvilket legemiddel som faktisk ble utlevert basert på en resept.
* *ReseptKurv* er hovedenheten for kommunikasjon mellom kjedesystemene og DIFA. En ReseptKurv representerer de legemidlene som skal utleveres samtidig til en pasient på apoteket. Normalt vil en pasient kun ha en ReseptKurv under ekspedering, men en kunde kan få utlevert flere reseptkurver, for eksempel for barn eller veterinærresepter.
* *ReseptVarsel* er en melding som DIFA genererer til farmasøyten. Eksempler kan være Interaksjonsvarsler, Endring av dosering, Dobbelforskrivning etc. Varsler vil ha alvorlighetsgrad der DIFA kan sette begrensninger rundt utlevering av alvorlige varsler.
* *ReseptUtleveringAksjon* dokumenterer en farmasøyts aksjon basert på et varsel. Ved alvorlige varsler kan DIFA kreve at farmasøyt dokumenterer en varsel for å tillate utleveringen
* *Legemiddel* er uformelt beskrevet i denne modellen. En Resept kan forskrives på virkestoff, merkevare eller pakning, mens en utlevering alltid vil være på pakningsnivå. DIFA vil informere kjedesystemet om alle alternative legemiddelpakninger på resepten basert på byttegruppe (ved forskrivning på pakning) eller ATC-kode (ved forskrivning på virkestoff).
* *Helseperson* er en helseperson definert i Helsepersonellregisteret med tilhørende godkjenninger. Dette kan være forskriver (lege, veterinær etc) eller apotekansatt (apotektekniker eller farmasøyt)



Følgende figur illustrerer de viktigste begrepene i DIFA og hvordan de henger sammen. I grensesnittet mellom kjedesystemene og bransjesystemet ser leverandøren for seg at disse begrepene har engelske navn med utgangspunkt i HL7-standarder (se systemdesign for detaljer).

Et viktig poeng med DIFA er at man dokumenterer aksjonene som ble tatt basert på farmasøytiske varsler. Modellen må også synliggjøre de legemidlene som resepten kan utleveres for (byttegruppe mm).

Det mest uventede begrepet i modellen har vi i mangel på et bedre navn kalt "ReseptBunke". Alle resepter forskrevet av samme lege på samme dato inngår i samme "bunke". Reseptformidleren behandler dem under ett for en egenandelsperiode og DIFA må være bevisst på at en resept under ekspedering i en bunke kan skape krøll med egenandelen andre resepter i bunken. (Blåreseptforskriften § 8)

![Begrepsmodell](images/class-reseptur-konseptuell.png)

## Funksjonell flyt reseptur

![Funksjonell flyt for reseptur](reseptur.png)

Denne flyten illustrerer hvordan aktørene interagerer med systemet under reseptbehandling. Stegene som blir utført av apotektekniker kan også utføres av farmasøyt, for å forenkle beskrivelsen skriver vi kun "apotekansatt" for disse stegene.

1. Pasientens fastlege registrerer en resept i Reseptformidleren vha sin EPJ
    * Variant: Lege utskriver resept til bruk i egen praksis (Forskrift om legemidler fra apotek, § 5-2)
    * Variant: Legen utskriver i eget navn for å verne pasient.
    * Variant: Legen skriver ut resept på papir. Se separat flyt.
2. Pasienten identifiserer seg på apotek og ber apotekansatt få resepten ekspedert
    * Apotekansatt finner kunde i folkeregisteret
    * Variant: Pasient bruker resept-id i stedet for legitimasjon for å identifisere seg
    * Variant: Person med registrert fullmakt henter resept på pasientens vegne
    * Variant: Resept til person uten fødselsnummer/D-nummer
    * Variant: Resepten bestilles som forsendelse over telefon eller elektronisk
    * Variant: Resepten hentes av institusjon eller hjemmehjelptjeneste (ekspederes som ordre)
    * Variant: Dersom pasienten er en multidosekunde varsles apotekansatt om dette. (Multidoseekspedering er ikke omfattet av denne beskrivelsen)
3. Apotekansatt henter reseptliste fra Reseptformidleren for pasienten via sitt POS og DIFA
4. Apotekansatt laster ned resepter som skal ekspederes fra Reseptformidleren
    * Variant: Systemet hindrer en resept som er under behandling i et annet apotek fra å ekspederes
    * Variant: Systemet gir apotekansatt varsel *før* nedlastning dersom pasientens resepthistorikk indikerer at ekspederingen kan gi interaksjoner, dobbelt forskrivning, doseendring eller nytt legemiddel
    * Variant: Systemet formidler varsler som er registrert på legemiddelet fra legemiddelverket eller FarmaLogg
5. Apotekansatt vurderer bytte til et rimeligere legemiddel i samme byttegruppe
    * Variant: Lege, apotek eller pasient kan reservere seg mot generisk bytte
    * Variant: Farmasøyt bestemmer intervensjon i form av endret dosering, vare, refusjonshjemmel eller personopplysninger
7. Apotekansatt skriver ut reseptetikett og foretar teknisk kontroll ved å lese av strekkode på pakning og etikett
    * Variant: Apotekansatt kan skrive ut avstemplingslapp eller navnelapp for senere avhenting
    * Variant: Systemet sjekker avlest QR-kode på pakning mot forfalskningsregisteret
8. Farmasøyt registrerer aksjoner for advarsler farmasøytkontroll på apotekansatts arbeidsstasjon eller på separat arbeidsstasjon eller mobil enhet
    * Systemet vil avvise utleveringen dersom nødvendige aksjoner ikke er dokumentert
9. Pasient signerer og betaler for alle reseptene i reseptkurven og mottar legemidlene
9. Systemet registrerer utleveringen av alle reseptene i reseptkurven i Reseptformidlere og sender eventuelt refusjonskrav til HELFO. Reseptformidleren markerer resepten som ekspedert dersom det ikke gjenstår iterasjoner.

### Spesialtilfeller

* Dersom DIFA eller Reseptformidleren ikke kan nåes fra apotekets POS kan apotekansatt etterregistrere resept som nødresept
* Utlevering på papirresept: Legemidlene på resepten som blir utlevert blir registrert av apotekansatt manuelt. Resepten kan om nødvendig registreres etter utlevering. Pasientens signatur kan registreres fra scannet dokument i stedet for signaturpad.
* Nødekspedering: Farmasøyt oppføres som rekvirent?
* Pasient returnerer vare for kreditering: M10 og M18 med negative beløp og antall skal generes. Egenandel på M10 for perioden må reduseres.
* Apotekansatt benytter DIFA GUI for resepthåndtering (fullstendig scenario)
* Veterinærresept. Sterk identifisert person kun ved A/B resept. Papirresept (system innenfor langsiktig målbilde)
* Legen sender ekspederingsanmodning til et spesifikt apotek
* Anbrudd - apotekansatt registrerer anbrudd og DIFA vedlikeholder et anbruddsregister
* Pasient ekspederer resept via nettapotek (fullstendig scenario, men ikke i denne leveransen)
* Multidose-apotek ekspederer legemidler i bruk for pasient (fullstendig scenario, men ikke i denne leveransen)


## Funksjonell flyt refusjon

1. Apotekansatt registrerer reseptlevering for en resept med refusjonshjemmel
2. Systemet beregner korrekt refusjon
    a. Systemet slår opp egenandelfritak for pasient fra NAV
    b. Pasienter uten fødselsnummer eller D-nummer, inkludert EØS borgere
    c. Pasienten kan ikke være død
    d. Legemidler med forhåndsgodkjent refusjon (§ 2)
    e. Individuell søknad (§ 3 - M3, M14, M15, M20)
    f. § 4 (legemidler mot smittsomme sykdommer utlevert til personer som ikke er medlem i folketrygden)
    g. Medisinsk forbruksmatriell (§ 5) ihht pris- og produktliste fra Helsedirektoratet
    h. Prevensjonsmidler til jenter under 20 år
    i. Byttereservasjon til vare som er over trinnpris eller AUP
    j. Refusjon for preparater uten refusjonspris eller maks AUP
    k. Varsle ved AIP høyere enn akseptabelt gevinstdeling
    l. Refusjoner for tjenester ytet i apotek (inhalasjonsveiledning, LAR, i fremtiden medisinstart)
    m. Tak på egenandel per resept per tremånedersperiode
    n. Arbeidspris (Rundskriv 7/2008 fra Legemiddelverket (pkt. 6)) - gjelder tilbereding av antibiotikamiksturer
    o. Andre refusjonsinstanser enn NAV (jernbaneverket)
    p. H-resept - betales av helseforetakene ("men kan brukes utenfor sykehus") - separat M18
    q. Verneplikt - egenandelsfritak
    r. Yrkesskade ?? § 5-25, brystproteser
3. Pasienten kan nå motta legemidlene og forlate apoteket
4. HELFO sender oppgjørsresultat når kravet er validert (typisk noen få minutter)
    * Unntak: Dersom HELFO avviser kravet kan apotekmedarbeider korrigere kravet og sende på nytt
    * Unntak: Dersom HELFO avviser kravet vil tjenesteleverandøren håndtere dette som en B-feil
5. Kjedens regnskapssystem henter status fra systemet på refusjonskrav og oppretter fordringer [eller er dette bedre beskrevet som faktura?]
6. HELFO sender utbetalingsmelding når betalingen er utført
7. Kjedens regnskapssystem henter status fra systemet og registrerer betaling av fordringen


## Funksjonell flyt farmasøytiske tjenester

![Funksjonell flyt for inhalasjonsveiledning](inhalasjonsveiledning.png)

1. Ved utlevering av legemiddel varsler systemet apotekansatt om at det kan være aktuelt å yte relevant tjeneste basert på resept eller tjenestehistorikk (NB: Er dette tillatt ifg personvernforordningen?)
   * Inhalasjonsveiledning
   * Foreslått inkludert i 1.0: Medisinstart
   * Foreslått inkludert i 1.0: LAR
   * Mulighet: Legemiddelgjennomgang
   * Mulighet: Legemiddelveiledning
   * Mulighet: Vaksinering
   * Mulighet: Føflekkscanning
2. Pasienten ønsker å motta tjenesten
3. Apotekansatt får opp veiledning for utførelse av tjenesten
    * Veiledningen kan være i form av et spørreskjema som fylles ut i samråd med pasient og med linker til mer omfattende dokumentasjon
4. Apotekansatt registrerer detaljer om tjenesten som ble ytet
5. Pasient signerer for tjenesten
6. Apotekansatt avslutter tjenesten i systemet
7. Tjenesten blir dokumentert i apotekets journal
    * Pasient kan på forespørsel få innsyn i egne journalopplysninger
    * I en senere versjon kan journalen leveres til helsenorge.no
8. Systemet registrerer refusjonskrav mot HELFO
    * Refusjonskravet kan være avhengig av detaljer om tjenesten. For eksempel, for LAR: Overvåket inntak av flytende metadon har en sats på 36,75 kr, mens Buprenorfin tabletter har 98 kr. For Medisinstart: Ulik refusjonspris per oppfølgingspunkt


## Pasientjournal

Både resepthistorikk og journal for farmasøytiske tjenester er underlagt pasientjournallovens § 3.Saklig virkeområde: "Loven gjelder all behandling av helseopplysninger som er nødvendig for å yte, administrere eller kvalitetsikre helsehjelp til enkeltpersoner." [Merk imidlertid Pasientjournalforskriften § 2.(Unntak for apotek)]

Disse journalene inneholder også personlig identifiserbar informasjon og vil dermed være underlagt Personvernforordningen (som erstatter ditto lov, forskrift og direktiv).

Disse to regelsettene er i stor grad overlappende og innebærer en del funksjonalitet og aktiviteter som vil inngå i product backlog:

* All tilgang til journalene vil loggføres (pasientjournalloven § 16. Forbud mot urettmessig tilegnelse av helseopplysninger)
* Når en bruker slår opp pasientopplysninger vil DIFA kreve at brukeren registrerer årsaken til oppslaget samt legitimasjon for den som forespurte oppslaget om relevant
    * Systemet vil holde tilbake informasjon som den som gjør oppslaget ikke skal ha (for eksempel ved vergeinnsynsperre)
* Bruker med relevant autorisasjon skal ettergå tilgangslogg
    * Variant: Bruker kan søke opp kritiske hendelser som kansellerte ekspederinger, nødekspedering og intervensjon
* Pasienter må kunne få utlevert medisiner uten å bli registrert (pasientjournalloven § 17. Rett til å motsette seg behandling av helseopplysninger). MÅ DISKUTERES HVORDAN DET SKAL LØSES FUNKSJONELT.
* Bruker med relevant autorisasjon kan ta ut informasjon om en pasient på pasientens forespørsel (pasientjournalloven § 18. Informasjon og innsyn)
* Bruker med relevant autorisasjon kan rette og sperre informasjon om en pasient på pasientens forespørsel (personopplysningsloven § 27. Retting av mangelfulle personopplysninger)
* Pasientjournalen vil oppdage og forhindre at brukere forsøker å hente ut store mengder med data på kort tid ("resource governor")
* Som en del av målbilde vil DIFA avlevere informasjon om en pasient til helsenorge.no slik at pasienten kan være selvbetjent på innsyn (pasientjournalloven § 18. Informasjon og innsyn)


## Rapporter

Alle rapporteringsgrensesnitt må renses for personlig identifiserbar informasjon. Personopplysningsloven § 11 krever at personopplysninger ikke lagres lengre enn nødvendig. Siden rapporter vil eksporteres fra systemet må disse anonymiseres for å oppfylle lovkravet.

Rapportene vil være basert på reseptutleveringer, men vil ikke inneholde kobling til person. I stedet vil fødselsnummer være erstattet med en sikker generert nøkkel. Nøkkelen vil ikke være et sekvensnummer ettersom dette vil fortelle noe om når den var generert. Der det ikke er påkrevd vil informasjon om hvilket apotek som foretok ekspederingen fjernes. Der det ikke er mulig vil apotek fortrinnsvis erstattes med fylke eller kommune/bydel.

TODO: Dette blir helt annerledes


1. DIFA overfører periodisk grunnlag der personopplysninger er fjernet til rapporteringsdatabase
2. System som bruker rapporterte data henter ned rapporter etter eget behov
    * Systemet autentiseres med en oauth2 bearer token fra kjedes Identity Provider (Active Directory eller tilsvarende)
    * Systemet autoriseres basert på systemets rolle (tildelt fra kjede)
    * Systemet kan spesifisere at kun data etter et gitt tidspunkt skal returneres
3. DIFA sender rapporter på email periodisk til mottakere som har behov for dette

Rapportene vil være på maskinlesbare formater.

Rapportmottakere:   

* Folkehelseinstituttet
* Mattilsynet
* Helsetilsynet

Følgende rapporter viser apotekenes fagarbeid

* Interaksjonsstatistikk
* Ekspedisjonsstatistikk
* Gjennomføringsevne farmasøytiske tjenester (se også under farmasøytiske tjenester)

Kundebetaling resept: Må ikke behovet for dette dokumenteres?

Følgende rapporter inngår i journalhåndtering og dekkes av den delen av løsningsbeskrivelse:

* Innsynsrapport
* Kansellerte reseptformidleroppslag
* Intervensjoner


### Bransjestatistikk

DIFA inneholder kun legemiddeldelen av bransjestatistikken. Leverandøren ser for seg to måter dette kan løses:

a. Et uavhengig system henter datagrunnlag fra DIFA og sammenstiller med grunnlaget fra kjedene.
b. DIFA henter datagrunnlag fra kjedene og sammenstiller dette til en felles rapport



## Brukergrensesnitt

### Foreslått inkludert brukergrensesnitt: Farmasøytkontroll, farmasøytiske tjenester, journal

* Farmasøyt kan hente opp en liste over reseptkurver for valgt apotek som er klare for farmasøytkontroll
* Farmasøyt kan utforske varsler om legemidlene i en reseptkurv og notere aksjoner på disse
* Farmasøyt kan godkjenne reseptkurv for utlevering
* Opsjon: Farmasøyt mottar et varsel når en resept er klar for farmasøytkontroll

* Farmasøyt kan gjennomføre farmasøytiske tjenester i et effektivt brukergrensesnitt

* Administrator kan gjennomføre alle journalrelaterte operasjoner i et egnet brukergrensesnitt

### Brukersnitt for test og demo: Reseptekspedering test-GUI

Merk at farmasøytkontroll foreslås levert som en del av kjerneløsningen

1. Apotekansatt søker opp person
2. Apotekansatt viser reseptliste for person
3. Apotekansatt velger resepter for ekspedering
4. Apotekansatt gjennomfører generisk bytte
5. Apotekansatt viser varsler, legemiddelinformasjon og ekspederingstøtte
6. Apotekansatt gjennomfører teknisk kontroll
7. Apotekansatt registrerer signatur og utleverer resept

### Opsjon: Reseptekspedering GUI

Testbrukergrensesnittet vil danne et godt grunnlag for et fullstendig brukergrensesnitt for å ekspedere resepter. Det vil kreve noen integrasjonsaktiviteter samt brukergrensesnittforbedringer for å gjøre det bruksverdig av apotekansatt til daglig arbeid. Nøyaktig omfang av dette arbeidet vil være avhengig av rammevilkår for kjedene som eventuelt ønsker denne muligheten.

Leverandøren kan levere et pristilbud på nødvendige tilpasninger som et avrop på rammeavtalen. Aktuelle opppgaver vil være:

* Apotekansatt får se priser og beholdning for varer under gjennomføring av generisk bytte
* DIFA skal støtte nødvendig interaksjon med kjeders POS
* DIFA skal ha en effektiv og innbydende visuell karakter
* DIFA brukergrensesnittet skal brukertestes


## Krav til tjenesten DIFA

### Sikkerhet

DIFA løsningen oppbevarer store mengder pasientopplysninger og det er kritisk at prosjektet har fokus på både sikkerhet og personvern. Et slikt fokus skjer ikke av seg selv og prosjektet vil derfor planlegge konkrete tiltak og sikkerhetsmessige gjennomganger.

Sikkerhetskravene i prosjektet tar utgangspunkt i Normen faktaark 38 og OWASP ASVS versjon 3.

* Tilgangsstyring (helsepersonelloven § 48): Apotekkjedene vil være ansvarlig for sikker og korrekt *identifisering* av bruker opp og angivelse av HPR nr. Bransjeløsning vil være ansvarlig for rettighetskontroll, spesielt opp mot Helsepersonalregisteret.
* Sikker kommunikasjon med eHelse: Systemet vil signere meldinger til HELFO og RF i henhold til Rammeverk for elektronisk meldingsutveksling i helsevesenet
    * Systemet vil behandle private nøkler til virksomhetssertifikater uten at forretningstjenester har direkte tilgang til nøklene (OWASP ASV 7.11)
    * Systemet vil tillate trygg oppdatering av virksomhetssertifikater
* Personopplysninger vil så langt det er mulig lagres i kryptert form. (OWASP ASVS 7.29). Sikker oppbevaring av data (apotekloven, personopplysningsloven): Oppbevaring i EU (personopplysningsloven § 29). Sikkerhetstiltak ihht personopplysningsloven § 13.
* Personopplysninger vil automatisk fjernes fra systemet etter 12 måneder. Vinduet for historiske data vil være mulig å endre.

Prosjektet vil gjennomføre sikkerhetskontroller ved hver leveranse:

* Prosjektet vil gjøre sikkerhetsrevisjon ved hver viktige leveranse. Revisjonen vil spesielt vektlegge:
    * Systemet er satt opp og konfigurert i henhold til korrekte sonemodeller
    * Normen faktaark 6b sjekkliste B
    * OWASP ASVS V3 - Session Management
    * OWASP ASVS V4 - Access controll
    * OWASP ASVS V5 - Malicious input handling
    * OWASP ASVS V7 - Cryptography at rest
    * OWASP ASVS V8 - Error handling and logging
    * OWASP ASVS V9 - Data protection
    * OWASP ASVS V15 - Business logic
    * OWASP ASVS V18 - Web services
* Prosjektet vil sette opp verktøy for statisk analyse av koden med spesielt fokus på sikkerhetsmessige svakheter
* Privacy Impact Assessment i henhold til standardisert veileder
* Teknisk orientert trusselmodellering der hele teamet deltar

DIFA vil stille krav til kjedenes Identity Provider:

* Passordkrav ihht normen (faktaark 6b)
* Nivå 3 sikkerhet der det er nødvendig for å sikre pasientvern (eller nivå 4)


### Dokumentasjon og testbarhet

* Systemets API vil dokumenteres i Swagger UI som også kan brukes for å teste alle tjenestekall mot et testsystem
* Systemet vil leveres med et brukergrensesnitt som kan brukes til å demonstrere og verifisere oppførsel uten å være koblet mot et kjedesystem
* Testsystemet vil være satt opp med Identity Provider (for eksempel Active Directory) for en fiktiv apotekkjede
* Testsystemet vil være satt opp med en statisk kopi av FarmaLogg og HPR
* Testsystemet vil leveres med en simulator for kommunikasjon med RF, HELFO og NAV
* Prosjektet vil sette opp verktøy for automatisert lasttesting av alle APIer
* Prosjektet vil understøtte avbruddstest i henhold til driftsavtalen


### Drift, vedlikehold og oppdateringer:

* DIFA vil kunne motta datadump-filer fra eksisterende FarmaPro instanser. Systemet vil effektivt håndtere fulleksport minst en gang i døgnet, slik at FarmaPro kan laste opp filer i en lang overgangsperiode. Eksportfilene vil være lagret kryptert der den private nøkkelen kun er kjent av DIFA-systemet.
* Administrator kan registrere endringer satser for refusjonsordninger som gjelder fra angitte datoer (alternativt: Informasjonen hentes fra et annet system)
* Systemet må kunne oppdatere lister over legemidler og produkter som er godkjent for refusjon
* SLA overvåking: Systemadministrator og systemeier vil kunne se på statistikk over alle tjenestekall per tjeneste og aggregert på tjenesteområder. Statistikken vil inneholde bruksfrekvens og responstidstatistikk og understøtte krav om ytelse, skalerbarhet og oppetid. Statistikk vil kunne vises med oppløsning på månedsnivå og dagsnivå.
    * Dersom Leverandøren også leverer drift faller dette inn under driftsavtalen. Dersom drift skal leveres av en tredjepart vil driftsleverandør være ansvarlig for å produsere rapporter etter utviklingsleverandørens spesifikasjon
* Ytelse og skalerbarhet: Applikasjonen vil håndtere 500 http requester per sekund på topp. 95% av requestene der bruker må vente på svar skal respondere innen 0,5 sekunder.
    * Kunden spesifisere 200.000 resepter om dagen. Av mangel på mer detaljert statistikk antar leverandøren konservativt at halvparten av dette er i løpet av en time. Det gir opphav til en toppbelastning på 30 resepter per sekund. Arkitekturen legger opp til cirka 12 http requester per sekund med opptil 10 sql queries per request.
* Tjenesten vil ha 99,7% oppetid målt på månedsbasis med 30 minutter RPO. Opptid vil måles fra point-of-delivery for DIFA.
    * Nedetid på eHelse og andre nødvendige integrasjoner skal trekkes fra nedetidsberegningen
    * Dersom teknisk drift leveres av tredjepart skal nedetid på nettverk og servere trekkes fra nedetidsberegningen. Dersom leverandørens opsjon om drift innløses inngår nedetid på nettverk og servere som leverandøren er ansvarlig for i nedetidsberegningen
    * Leverandøren anbefaler å utløse driftsopsjonen for å sikre en fullstendig tjenesteleveranse
* Enkeltnoder i applikasjonen vil kunne fjernes og legges til uten å berøre brukerne. Applikasjonsservere må være uavhengig av sesjonstilstand.
* Nye versjoner av systemet vil kunne produksjonsettes uten å berøre brukere
    * Fatale feil ved nye versjoner vil kunne oppdages raskt og rulles tilbake med påvirkning på et minimum antall apotek
* Feilhåndtering: Systemadministrator vil varsles dersom en kritisk hendelse inntreffer i logger
    * Systemadministrator vil kunne se årsaken til vanlige problemer et sentral loggverktøy (Splunk)
* Ved nye leveranser vil det dokumenteres hvordan bakoverkompabilitet og fremoverkompabilitet er støttet. Viktige vurderinger i den forbindelse:
    * Klienten angir API versjon som en del av URL. DIFA vil støtte eldre versjoner så langt kjedene er avhengig av dem og de lar seg støtte gitt forholdene til reseptformidleren.
    * Nye leveranser kan legge til output-felter og ikke-påkrevde input-felter på et eksisterende API. Teknisk sagt vil nye leveranser av eksisterende API støtte Liskovs Subtitution Principle (LSP)
    * Endringer i API som endrer navn på felter, fjerner felter eller legger til nye påkrevde input-felter vil leveres i form av en ny API versjon.
    * Dersom kommende endringer i Reseptformidleren krever ikke-bakoverkompatible endringer i DIFA vil DIFA gjøre det mulig å utføre endringer i kjedesystemene _før_ endringene trer i kraft i Reseptformidlere for å gi kjedene fleksibilitet


## Utfordrende områder

### Sertifikathåndtering

Se kapittelet om Sikkerhetsarkitektur. Denne løsningen må godkjennes av direktoratet for e-Helse. Det vil være vanskelig å se for seg at DIFA kan levere et forenklende API dersom e-Helse ikke godkjenner løsningen.


### Beregning av interaksjoner basert på meldinger fra Reseptformidleren

For å forbedre pasientsikkerheten samtidig som man ivaretar personvernet ville det være en fordel om Reseptformidleren kunne endres til å inkludere legemiddelkode i Reseptlista (M9.2)

Av hensyn til personvern og master data er det en fordel om DIFA benytter reseptformidleren fremfor egen resepthistorikk. Av hensyn til pasientsikkerheten er det viktig å kunne se interaksjon mellom tidligere resepter og resepter som er til ekspedering.

Reseptliste-meldingen fra Reseptformidleren (M9.2) inneholder historiske reseptutleveringer. Men meldingen inneholder ikke forskrivningsinformasjonen, kun NavnFormStyrke, som er "Sammenstilling av varenavn, legemiddelform for preparatet og styrke.»

Dersom Reseptformidleren endres til å la ReseptInfo i reseptliste inkludere Forskrivning (fra ReseptDokLegemiddel) som var angitt i Resepten (M1) (eventuelt ReseptDokHandelsvare for relevante resepter) samt inkludere historiske resepter som kan være helsemessig relevante, så vil DIFA kunne gjøre interaksjonskontroll og varsling med Reseptlista som input (i tillegg til FarmaLogg).

Alternativt må DIFA laste ned reseptene (M9.3, M9.4) eller bruke resepthistorikk for å gjøre denne beregningen.


## Vedlegg: Utkast til product backlog

Se eget regneark for utkast til produkt backlog for versjon 0.1, 0.2, 0.3 og 1.0.


# Systemdesign

Strukturen i systemdesignet er lagt opp etter 4C prinsippet fra Simon Browns bok "Software Architecture for Developer". Innfallsvinkelen beskriver systemet først fra en overordnet kontekst (første C - Context), til elementene som skal kjøre på en driftsplattform (andre C - container), til komponentene og tjenestene som realiserer funksjonaliteten (tredje C - components) til informasjonsmodellen som beskriver forretningskonseptene (siste C - classes). I vår bruk kan Context og Containers sees som uttømmende, mens Components viser prinsippene rundt referansearkitekturen med spesiell fokus på viktige aspekter rundt sikkerhet og kommunikasjon.

![C4-modellen for systemdesign](c4-model.png)


## Overordnede arkitekturprinsipper

### Prinsipper i behovsbeskrivelsen (fra kunde)

* Fleksibilitet. Bransjeløsningen skal utformes slik at den ikke fremstår som begrensende for endringer i apotekenes arbeidsprosesser, innhold, organisering, eierskap og infrastruktur. Time to market for ny funksjonalitet skal være kort.
* Tjenesteorientering. Bransjeløsningen skal være tjenesteorientert og sammensatt av løst koblede komponenter med åpne, standardiserte grensesnitt. Funksjonalitet og ytelsesnivå skal være hovedhensyn ved utvikling av bransjeløsningen.
* Samhandling og interoperabilitet. Bransjeløsningen skal samhandle effektivt med apotekenes egne, myndigheters og andre relevante virksomheters IT-løsninger. Bransjeløsningen skal understøtte interoperabilitet på organisatorisk, semantisk og teknisk nivå.
* Kvalitet. Teknisk og faglig kvalitet skal bygges inn i bransjeløsningens prosesser og funksjonalitet. Bransjeløsningen skal bygges på en måte som sikrer at data som eies av bransjeløsningen, data som registreres i bransjeløsningen, og data som brukes i bransjeløsningen valideres tidlig og er av høy kvalitet. 
* Brukervennlighet og effektivitet. Bransjeløsningen skal være intuitiv og lede de ansatte gjennom prosessene på en effektiv måte. Brukervennlighet innebærer at bransjeløsningen skal oppleves som enkel å bruke, gjøre apotekansatte i stand til å utnytte sin kompetanse optimalt, og ikke oppleves som en hindring i arbeidet.


### Leverandørens forståelse av arkitekturprinsippene

* Fleksibilitet: DIFA er et API som eksponeres til apotekene. API-et vil ha som hovedprinsipp at det er tjenestekallene er gjenspeiler forretningsoperasjoner og kan benyttes i en fleksibel rekkefølge. Systemet vil benytte en infrastruktur som tillater utrulling uten opplevd nedetid. Fleksibiliteten er imidlertid begrenset til mulighetsrommet i eResept
* Tjenesteorientering: DIFA vil definere et grensesnitt dokumentert med Swagger UI. Alle tjenester vil være underlagt målinger av kallfrekvens og responstid.
* Samhandling. DIFA vil legge til grunn internasjonale standarder for navngiving og struktur i API, spesielt HL7 standarder. DIFA vil ta førerrollen med å definere stabile meldingsformater som bransjen kan forholde seg til og som understøtter myndighetskrav
* Kvalitet. DIFA vil validere refusjoner og aksjoner på farmasøytiske varsler og hindre utleveringer der refusjon ikke vil bli godkjent av HELFO. Avviste refusjonskrav vil registreres som B-feil i DIFA forvaltning.
* Brukervennlighet og effektivitet. DIFA vil ikke investere i et avansert brukergrensesnitt. I stedet vil kravet om fleksibilitet understøtte kjedenes evne til å levere brukervennlige og effektive løsninger. (Dette kan endres dersom omfanget av brukergrensesnitt endres)


## Systemlandskap og integrasjoner (4C - Context)

Dette kapitlet tar for seg arkitektur på abstraksjonsnivået context i C4-modellen, og gir en oversikt over andre systemer og aktører som DIFA skal samhandle med. Kapitlet skal gi en forståelse for hvilke prosesser DIFA inngår i og understøtter.

Kapittelet søker å gi oversikt over systemlandskap og meldingsflyt mellom systemer og personer i de viktigste scenarioene for DIFA og danner grunnlaget for å få oversiktsbildet av DIFA.

* Systemlandskap for DIFA under resepthåndtering
* Et tilsvarende landskap under testing av DIFA i isolasjon
* Meldingsflyt mellom personer og systemer når apotekansatt ekspederer en e-resept
* Meldingsflyt mellom personer og systemer når apotekansatt ekspederer en papirresept
* Meldingsflyt mellom systemer under refusjonsbehandling
* Systemlandskap for DIFA ved levering av farmasøytiske tjenester
* Meldingsflyt mellom personer og systemer når apotekansatt utfører farmasøytisk tjeneste


### Kontekst for reseptur og refusjon

Systemskisse for reseptur inkludert refusjon viser hvordan aktørene i systemet samhandler med DIFA i sentrum.

I målbildet av systemet formidler DIFA kommunikasjonen mellom kjedens systemer (POS, ERP, Nettapotek, Multidose) og systemene som inngår i det norske helsevesenet (Reseptformidlere, HELFO, NAV, HPR, Farmalogg).

Figuren tar kun høyde for normalflyten for reseptur og for eksempel ekspederingsanmodning er utelatt. I figuren har vi brukt lysegrønne bokser for kjedesystemer, mørkegrønn boks for DIFA, røde bokser for kritiske integrasjonspunkter og gule bokser for andre systemintegrasjoner.

![Kontekst for reseptur og refusjon](images/context-reseptur.png)

![Kontekst under testing](images/context-test.png)

### Systemlandskap ved bruk av DIFA GUI opsjon

![Kontekst ved bruk av GUI (opsjon)](images/context-gui.png)

Noen kjeder kan ønske å benytte brukergrensesnitt for reseptur levert som en del av DIFA. I dette tilfelle vil apotekansatt benytte dette systemet for å forberede resepten og POS for å motta betaling og signatur for resepten. POS vil varsle DIFA om at betalingen er komplett slik at DIFA kan formidle utleveringen til Reseptformidleren og sende Refusjonskrav til HELFO.

Leverandøren forventer at kjeder som bruker DIFA GUI for resepthåndtering vil kunne unngå å måtte teste sine systemer med direktoratet for e-Helse.


### Meldingsflyt under ekspedering av e-resept

Når DIFA returnere resepter for ekspedering samles all informasjonen som kjeden trenger for å føre dialogen med apotekansatt i ett kall. Tilsvarende vil DIFA validere alle aspekter ved utleveringsmeldingen.

![Overordnet flyt for reseptur](images/context-dynamic-reseptur.png)

### Meldingsflyt under ekspedering av papirresept

![Overordnet flyt for papirreseptur](images/context-papir-reseptur.png)

### Meldingsflyt under refusjonsbehandling

![Overordnet flyt for refusjon](images/context-dynamic-refusjon.png)

### Systemlandskap for farmasøytiske tjenester

Systemskisse for farmasøytiske tjenester viser samhandling mellom pasient, farmasøyt og DIFA under gjennomføring av farmasøytiske tjenester.

DIFA vil veilede, informere, registrere og håndtere refusjon for farmasøytiske tjenester levert i apotek. De mest aktuelle tjenestene er inhalasjonsveiledning og medisinstart, ettersom det er tjenester som allerede leveres i dag. Legemiddel-assistert rehabilitering (LAR) er også en aktuell tjeneste ettersom den har fått refusjonsordning.

Kjeden kan velge om farmasøyt skal registrere tjenesten i et brukergrensesnitt i DIFA eller i kjedens eget system. Leverandøren anser at det er lite interaksjon med kjedens andre aktiviteter og at det derfor er mest aktuelt å benytte et brukergrensesnitt i DIFA.

![Kontekst for farmasøytiske tjenester](images/context-apotektjenester.png)

### Meldingsflyt ved gjennomføring av farmasøytisk tjeneste (inhalasjonsveiledning)

![Overordnet flyt for farmasøytiske tjenester (brukergrensesnitt i DIFA)](images/context-dynamic-apotektjenester.png)


### Integrasjonsoversikt

Leverandøren foreslår følgende kommunikasjonsmekanismer med DIFA:

Kommunikasjonen mellom kjedesystem og DIFA foreslår leverandøren som JSON baserte REST API'er som autentiseres med OpenID connect/Oauth2.  Kommunikasjonen vil foregå over HTTPS forbindelse med 2-veis SSL sertifikater.

Dette er moderne, enkle og utbredte standarder og det er sannsynlig at det vil være det enkleste for kjedene å håndtere. Det mest aktuelle alternativet vil være å eksponere ebXML/KITH standarden for e-Helse, men dette vil antageligvis være mer krevende for kjedene å forholde seg til og vil kreve at kjedene kun benytter norskspråkling teknisk kompetanse til å integrere med DIFA.

I grensesnittene med Reseptformidleren og HELFO vil DIFA benytte standardene fra direktoratet for e-Helse. Leverandøren ser det ikke som aktuelt å utfordre disse standardene. Grensesnittene mot Reseptformidleren og HELFO er beskrevet på https://ehelse.no/e-resept-kjernejournal-og-helsenorgeno/e-resept, samt https://ehelse.no/ebxml-rammeverk-his-10372011

Grensesnitt mot Helsepersonellregister er beskrevet på https://www.nhn.no/hjelp-og-brukerstotte/personregisteret/andreressurser/Introduksjon-til-integrasjon-med-registrene.pdf

I grensesnittet mot FarmaLogg foreslår leverandøren å benytte filformatet som er enklest for FarmaLogg å produsere for å unngå å pålegge FarmaLogg unødig arbeid. Leverandøren foreslår at FarmaLogg utvikler et HTTPS grensesnitt der DIFA kan hente ned FarmaLoggs informasjon etter behov uten at FarmaLogg trenger å etablere jobber for å distribuere dette.


| Hvem                 | Hva                  | Hvordan                                     |
|----------------------|----------------------|---------------------------------------------|
| POS → DIFA           | Resepturekspedering  | JSON POST basert API autentisert med oauth2 |
| Nettapotek → DIFA    | Resepturekspedering  | JSON POST basert API autentisert med oauth2 |
| Multidose  → DIFA    | Resepturekspedering  | JSON POST basert API autentisert med oauth2 |
| ERP  → DIFA          | Refusjonsstatus      | JSON GET basert API autentisert med oauth2 på applikasjonsnivå |
| Reseptformidler      | Resepturekspedering  | M9.1, M9.2, M9.3, M9.4 med ebXML over SOAP  |
| HELFO                | Refusjonskrav        | M18 ebXML over SMTP                         |
| HELFO → DIFA         | Refusjonsstatus      | M22, M23 ebXML over SMTP                    |
| eHelse               | Egenandelstatus      | DIFA henter status for egenandelfritak      |
| FarmaLogg            | Legemidler           | DIFA henter. Løsningsforslag: HTTP GET XML  |
| Helsedir             | Helsepersonell       | DIFA henter HPR personregister              |
| helsenorge.no (fremtidig) | Pasientjournal  | DIFA leverer journalopplyninger. (Langsiktig målbilde) |




## Applikasjonsarkitektur

Applikasjonsarkitekturen samsvarer med "Container" nivået i arkitekturmodellen. Her vises alle komponenter som vil settes i drift og tjenestene som kjører på disse komponentene.

Applikasjonsarkitekturen har to innfallsvinkler: For det første illustrerer den de programvarekomponentene som må utvikles eller anskaffes. For det andre viser den hvilken interaksjon systemet vil ha med driftsplattformen.

Modulene i systemet vil gjenspeiler de funksjonelle hovedområdene: reseptur, farmasøytiske tjenester, pasientjournal, rapporter. I tillegg vil DIFA synkronisere informasjon fra Farmalogg, Helsepersonalregisteret, Helse enhetsregisteret, og et register til. Figuren viser også hvordan kommunikasjon foregår over Norsk helsenett.

På figuren vises DIFA i grønt, eksisterende systemer som anses som stabile i blått og systemer som vil etableres eller endres i parallell med DIFA i gult.

![Kjøretidsenheter i systemet (produksjon)](images/container.png)

### Systemoversikt

Systemoversikten viser omfanget av det som skal leveres og driftes som en del av DIFA. Tabellen under viser de komponentene som har selvstendig liv i produksjonsmiljøet. Oversikten vil justeres og oppdateres under behov under utvikling av systemet.

Leverandøren har beskrevet systemet som en Java-plattform. Man vurderer også SQL Server i stedet for PostgreSQL.

| System               | Teknologi | Beskrivelse                                     |
|----------------------|-----------|-------------------------------------------------|
| Reseptur             | Java      | HTTP API for kjedenes POS systemer              |
| Reseptur GUI         | Java      | Web grensesnitt for GUI opsjonen                |
| Resepthistorikk      | Java      | Web GUI og HTTP API for resepthistorikk         |
| Resepter             | PostgreSQL| Database som lagrer resepturinformasjon         |
| Journal              | PostgreSQL| Database som lagrer journal for farmasøytiske tjenester |
| Farmasøytisk         | Java      | Web GUI og HTTP API for Farmasøytiske tjenester |
| Legemidler           | PostgreSQL| Database med kopi av farmalogg                  |
| HPR                  | PostgreSQL| Database med kopi av helsepersonalregister      |
| LegemiddelSync       | Java      | Prosess som synkroniserer farmalogg             |
| HPRSync              | Java      | Prosess som synkroniserer HPR                   |
| KeyVault             | 3rd party | Fysisk eller virtual HSM (hardware security module) |

Alle systemer som har et grensesnitt med eksterne systemer inkluderer funksjonalitet for å autentisere og autorisere brukeren.

Miljøene i denne beskrivelsen utgjør Bilag 3 for SSA-D avtalen for DIFA. Ytterligere detaljer om driftsmiljøet kan finnes i Bilag 2 for SSA-D.

### Maskinvare og nettverk som inngår i DIFA

DIFA må fungere med høy ytelse og feiltoleranse. Figuren viser at DIFA kjører på flere fysiske lokasjoner med replikering. På hver lokasjon kjører flere kopier av programvaren for å håndtere last og oppnå feiltoleranse. 

![Produksjonsmiljø](images/system-prod.png)

### Grensesnitt mellom programvare og driftsmiljø (PaaS-konsept)

DIFA vil leveres på OpenShift - en docker-basert cloud infrastruktur med en Java-basert arkitektur. OpenShift håndterer laget mellom virtuelle maskiner (VMWare) og Docker-containere. Utviklingsprosjektet vil levere docker images. Applikasjonsdrift (DevOps) vil være ansvarlig for å oppgradere og skalere docker-containere på hardware som er levert innen driftsavtalen.

![PaaS konsept](images/container-reference-paas.png)

Teknologier:

| Område                | Alternativer                  |
|-----------------------|-------------------------------|
| FrontEnd              | HTML, CSS, JavaScript         |
| View-rammeverk        | Vue.js                        |
| AJAX rammeverk        | SuperAgent                    |
| View test-rammeverk   | Mocha                         |
| Server API            | JSON over HTTPS POST          |
| API rammeverk         | Jetty                         |
| Autentisering         | ADAL4J                        |
| Serverside JSON       | json-buddy                    |
| Loggerammeverk        | logback                       |
| Databaserammeverk     | JDBC                          |
| Database              | PostgreSQL                    |

### Testmiljøer

For å understøtte de forskjellige testaktivitetene vil det være behov for et stort antall testmiljøer. De fleste av disse miljøene vil ha minimale ytelsesbehov og vil leve samme på samme VMWare-instans (unntatt stresstestmiljøet). Utviklingsprosjektet kan sette opp nye testmiljøer etter behov innenfor den infrastrukturen som er levert i forbindelse med prosjektet.

I de følgende underkapitler beskrives:
* Systemtestmiljø
* Kjedenes integrasjonsmiljø
* E-Helseintegrasjonstestmiljø
* Stresstestmiljø

#### Systemtestmiljø

Figuren viser at testmiljøet leveres med teststillas som tar plassen til reseptformidleren og Helfo. Testmiljøet bruker en kopi av legemiddeldata fra Farmalogg, og HPR-register fra testversjonen av H-dir. Helseenhetsregister fra produksjon benyttes.

![System og utviklingstest](images/container-test.png)

#### Kjedenes integrasjonstestmiljø

Figuren viser et integrasjonstestmiljø levert til en kjede. DIFA vil inkludere drift av et slik miljø for hver apotekkjede og kan sette opp flere miljøer på forespørsel. Hvert miljø vil normalt være skalert for funksjonell testing og ikke for stresstesting.

Miljøet leveres med simulatorer av RF og HELFO for å enkelt kunne sette opp scenarioer som kjedene ønsker å teste. Simulatorene kan styres via et web-grensesnitt og via et REST API.

Databasene og simulatorene i miljøet kan resettes gjennom et web-grensesnittet slik at kjedene kan få et forutsigbart testmiljø når de har behov for det.

![Kjedes integrasjonstest](images/container-test-kjede.png)

#### Akseptansetestmiljø for e-Helse

For å understøtte Direktoratet for e-Helse sin akseptansetest vil leverandøren sette opp et miljø med et testbrukergrensesnitt. Miljøet vil være koblet mot e-Helse sitt testmiljø for RF.

Leverandøren gjør imidlertid oppmerksom på at direktoratet for e-Helse antageligvis ønsker å teste alle endringer i kjedens brukergrensesnitt der dette berører resepthåndtering.

![e-Helse sertifisering](images/container-test-ehelse.png)

![Stress test](images/container-test-stress.png)


## Plattform & Teknologi

Dette kapittelet viser hvordan systemet er sett for seg implementert. Kapittelet er delt i to deler: Først beskriver dokumentet den sentrale funksjonaliteten knyttet til reseptur. Så beskriver vi generelle gjennomgripende implementasjon av sikkerhet og tilgangskontroll.

Funksjonaliteten knyttet til reseptur er beskrevet på tre måter: Den normale bruken der kjedens POS system eksponerer funksjonalitet gjennom DIFA API, brukergrensesnittopsjonen der resepturgrensesnittet i DIFA leser beholdning fra kjedens varesystem, og i brukergrensesnittet for test og demonstrasjon. Testbrukergrensesnittet vil fungere tilsvarende som brukergrensesnittopsjonen, men uten integrasjon med kjedens varesystem.

Leverandøren foreslår at Farmasøyt benytter DIFA GUI til å foreta farmasøytkontroll. DIFA vil implementere et høy og samtidig pragmatisk sikkerhetsnivå for farmasøytkontroll.

### Grensegang mellom kjedesystem og bransjesystem

Dette kapittelet beskriver overordnet API struktur mellom kjedes systemer og DIFA. Som en del av versjon 0.1 vil leverandøren detaljspesifisere API og levere en fungerende testversjon som kjedene kan bruke i sin testing i god tid før DIFA 1.0 leveres.

#### Apotekansatt ekspederer resept i kjedesystem med DIFA sitt API

Følgende figur viser hvordan hele scenarioet for reseptekspedering fungerer. *Figuren går over to sider.* Apotekansatt ekspederer resepten gjennom følgende syv steg:

1.  Apotekansatt søker opp pasienten i folkeregisteret
2.  Apotekansatt henter liste over resepter klart til ekspedering
3.  Apotekansatt starter ekspedering av et sett med resepter
4.  Apotekansatt gjennomfører generisk bytte (velger legemiddel)
5.  Apotekansatt oppdaterer reseptetikett og gjennomfører teknisk kontroll av pakning og etikett
6.  Farmasøyt gjennomfører farmasøytkontroll og dokumenterer eventuelle aksjoner som ble tatt på grunnlag av varsler
7.  Apotekansatt tar imot betaling og signatur og DIFA markerer resepten som utlevert

![Grensegang mellom kjedene og DIFA 1 av 2](images/container-gui-kjede-pos.png)

![Grensegang mellom kjedene og DIFA 2 av 2](images/container-gui-kjede-pos_001.png)

### Variant: Under test kan kan ekspedere resepter i DIFA sitt inkluderte test-GUI

![GUI for kjede uten opsjon 1 av 2](images/container-gui-kjede-noAPI.png)

![GUI for kjede uten opsjon 2 av 2](images/container-gui-kjede-noAPI.png)

### Opsjon: DIFA test-GUI kan utvides til å brukes i produksjon

Noen kjeder kan ønske å benytte et brukergrensesnitt for reseptur levert som en del av DIFA. Følgende skisse viser hvordan det kan fungere. Leverandøren vil spesielt gjøre oppmerksom på:

* Leverandøren ser for oss å levere DIFA som en web-løsning. Dette innebærer begrensede integrasjoner med periferiutstyr, spesielt etikettprinter og strekkodeleser, noe som kan medføre dårlig brukeropplevelse.
* Kjeder som ønsker dette må eksponere sine lager-systemer etter en protokoll spesifisert av DIFA
* Kjeder som ønsker dette må implementere integrasjon mellom sine POS systemer og DIFA for å hente og utlevere reseptkurver

![Brukergrensesnitt som benytter kjedesystem 1 av 2](images/container-gui-kjede-API.png)

![Brukergrensesnitt som benytter kjedesystem 2 av 2](images/container-gui-kjede-API_001.png)


### Referansearkitektur

Denne figuren vise navngiving av forskjellige elementer av løsningen.

![Referansearkitektur](images/component-reference-architecture.png)

### Implementasjon av et typisk scenario: Nedlastning av resepter

Følgende figur gir et dypdykk på hvordan ett steg i arbeidsflyten vil bli implementert internt i DIFA. Figuren viser:

1. Kjedesystemet ber DIFA om å starte en reseptekspedering
2. DIFA autentiserer at brukeren kjeden angir i tjenestekallet har lov til å ekspedere resepten
3. DIFA gjør nødvendig interaksjon med Reseptformidleren for å laste ned resepten
4. Under kommunikasjonen med reseptformidleren benytter DIFA apotekets virksomhetsnøkkel for å signere meldingen
5. DIFA beriker reseptekspederingsmeldingen med legemiddelinformasjon, byttegruppe etc.
6. DIFA lagrer reseptekspederingen i egen database
7. DIFA returnere reseptkurven til kjedesystemet

![Nedlastning av resepter](images/component-ekspeder-resept.png)

### Sikkerhetsarkitektur

#### Tilgangskontroll

DIFA vil ikke selv ha en brukerdatabase, men vil i stedet lene seg på kjedenes brukersystemer. DIFA vil integrere med disse via en standard oauth2 flyt. Der DIFA benyttes som et API bak kjedesystemet vil kjedesystemet utstede en JWT (JSON web token) og sender det som en Bearer token. Denne token er signert av kjedens Identify Provider (for eksempel Active Directory) og vil inneholde HPR-nr, hvilke HER-id brukeren er autorisert for og applikasjonsroller. DIFA vil kontrollere brukerens helsepersonalautorisasjon mot HPR basert på brukerens HPR-nr.

Når brukeren benytter DIFAs GUI vil en oauth2 code flow mot kjedens Identity Provider brukes for å overføre JWT til DIFA.

Kjedene er ansvarlig for å oppfylle passordkravene i Normen.

Ikke vist: For enkelte operasjoner vil DIFA kreve et høyere sikkerhetsnivå fra Identity Provider. Avhengig av kjedens oppsett kan dette innebære en 2-faktor autentisering med SMS-kode eller på annet vis.

![Tilgangskontroll (API)](images/component-sikkerhet-tilgangskontroll-api.png)

![Tilgangskontroll (GUI)](images/component-sikkerhet-tilgangskontroll-gui.png)

#### Tilgangslogg

For å oppfylle kravet i personvernforordningen og pasientjournalloven vil DIFA logge alle operasjoner der en bruker aksesserer pasientinformasjon.

![Tilgangslogg](images/component-sikkerhet-tilgangslogg.png)

#### Sertifikathåndtering

Dersom DIFA ikke skal eksponere kravet om ebXML-signering til kjedene, så må DIFA oppbevare virksomhetssertifikater på vegne av kjedene. Skissen viser hvordan sertifikatene administreres og brukes.

![Sertifikathåndtering](images/component-sikkerhet-sertifikat.png)

#### Overvåking

### Konvertering

* Leverandøren planlegger en Service Pack til FarmaPro 5.20 som vil eksportere data fra FarmaPro databasene i alle apotek som XML-filer (XML i databasen vil Base64-encodes), kryptere disse filene med et public key som eies av DIFA.
* FarmaPro serverne vil laste opp data hver natt til en tjeneste i DIFA. Denne tjenesten vil eksponeres over apoteknettet.
* DIFA vil lagre siste versjon av dataene i kryptert form i sikker sone
* DIFA vil automatisk laste inn endringene fra alle apoteker hver natt

For å unngå duplikater vil en av følgende strategier brukes:

* Når DIFA starter å lese et data fra et apotek vil DIFA hente opp en cache av den dataen som ligger i DIFA sin database under innlesningen.
* For data som har endringstidspunkt i FarmaPro ("updated_at" kolonne eller tilsvarende) vil DIFA ikke laste inn data som ikke har endret seg. FarmaPro vil fortsatt laste opp fullstendig datasett ved hver eksport for å kunne håndte innlesning på nytt av feilede data.
* For data som ikke har endringstidspunkt vil DIFA foreta en felt-for-felt sammenligning av (kortvarig cache av) data i DIFA databasen for å finne endringer

For å håndtere feil vil følgende strategi brukes:

* DIFA vil lagre hele raden som feiler i en egen tabell i databasen samt beskrivelse av feilen
* DIFA utviklingspersonell vil ettergå feilene og gjøre oppdateringer i koden for å sikre innlesning i fremtiden. Endringene vil normalt produksjonssettes i løpet av en uke
* Ettersom FarmaPro eksporterer et helt datasett vil neste innlesning etter neste produksjonsetting normalt fungere. 

Inntil et apotek går over til å benytte DIFA vil alle refusjonssvar gå til FarmaPro og sluttoppgjør vil naturlig inngå i neste datamigrering. Etter at et apotek har gått over til å benytte DIFA vil M23 komme til DIFA og DIFA vil oppdatere relevante rader. DIFA vil innføre tiltak for å unngå at neste dataeksport fra apoteket overskriver oppdateringen. Mekanismen for dette vil beskrives i detaljspesifikasjonen av versjon 0.3.


## Informasjonsarkitektur

Dette kapittelet beskriver sammenhengen mellom begrepene i datamodellen på et konseptuelt nivå. Kapittelet dekker Resepthåndtering, Legemidler og Farmasøytiske tjenester. Leverandøren har valgt å fokusere på disse områdene da vi anser de som de mest sentrale i løsningen.


I grensesnittet mellom kjedesystemene og bransjesystemet ser leverandøren for oss at disse begrepene har engelske navn med utgangspunkt i HL7-standarder, spesielt FHIR standarden og Common Product Model Følgende modell viser sentrale begreper i reseptur og hvordan de henger sammen.

Det er kun mulig å delvis benytte HL7 ettersom det ikke fullt ut støtter norske forhold. Spesifikt er refusjonsreglene i Norge og forskrivning på virkestoff ikke mulig å uttrykke direkte med modellen. DIFA vil derfor bruke en modell som understøtter e-Resept standarden, men med navngiving fra HL7.

Det er viktig for modellene å skille informasjon som krever behandling under personvernregler fra informasjon som kan behandles mer fritt. Dette vil gjøres ved at informasjonselementer som skal inngå i rapportering og statistikk vil ha en "token" i stedet for fødselsnummer eller annen identifiserbar nøkkel for pasienten. Denne blir generert på en måte der det ikke er mulig å utlede alder, kjønn, bosted eller andre aspekter ved personen. (Figurene er ikke oppdatert til å vise dette enda)

![Resepturbegreper](images/class-reseptur-logisk.png)

### Begrepsmodell for legemidler

«Ka e et lægemiddel?» spurte Marte om. Det er ikke så lett å svare på, for det er mange begreper som har nøyaktige betydninger men som kan være vanskelig å skille fra hverandre.

Figuren viser begrepene som inngår i definisjonen av et legemiddel og sammenhengen mellom dem. Figuren tar utgangspunkt i felleskatalogen/legemiddelverkets begrepsmodell oversatt til engelsk.

* Substance (virkestoff): Et kjemisk stoff som er kategorisert med en ATC kode. For eksempel metylfenidat (N06B A04)
* SubstanceConcentration: Virkestoff med styrke, for eksempel 5 mg/g eller 50 mg/tablett.
* Medication (legemiddelmerkevare): Et produkt solgt fra en produsent. For eksempel «Ritalin»
* MedicationPackaging (legemiddelpakning): En vare som kan utleveres i apotek. For eksempel «Ritalin, tabletter 10 mg, 200 stk»
* SubstitutionGroup (byttegruppe): En gruppe med legemidler som kan utleveres som alternativer til hverandre. For eksempel er «Medikinet, tabletter 10 mg, 100 stk» i byttegruppen til «Ritalin, tabletter 10 mg, 200 stk»

Begrepsmodelen har utelatt interaksjoner (substance interactions). Dette er fordi interaksjoner kan inntreffe på alle nivåer i Substance (virkestoff) hierarkiet og dermed er vanskelig å beskrive i en formell modell. For eksempel har substance Metylfenidat både en interaksjon med substance Moklobemid (N06B A04), men også med den farmakologiske undergruppen Hydantoinderivater (N03A B). Interaksjoner opererer også på grupper av substance, noe som også ikke egner seg for en grafisk fremstilling.

For å forenkle forståelsen inkluderer modellen ikke næringsmidler eller handelsvarer.

![Legemiddelbegreper](images/class-medication.png)

### Begrepsmodell for farmasøytiske tjenester

Leverandøren oppfatter farmasøytiske tjenester som veiledning gjennom et sett med steg og innsamling av informasjon om hvordan veiledningssamtalen gikk. Det kan være naturlig å bygge en fleksibel modell der steg, spørsmål og svaralternativer kan konfigureres dynamisk. Dette vil understøtte endringer i farmasøytiske tjenester og innføring av nye farmasøytiske tjenester uten systemendringer.

Informasjonsmodellen i figuren beskriver dette. Hvor vidt en såpass fleksibel modell er hensiktsmessig vil avgjøres under detaljspesifiseringen av farmasøytiske tjenester.

![Farmasøytiske tjenester (konseptuell)](images/class-farmasøytisk-konseptuell.png)

