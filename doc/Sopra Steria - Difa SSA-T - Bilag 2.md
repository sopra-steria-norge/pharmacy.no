
# Innledning

Løsningsbeskrivelsen av Difa er delt inn i fire områder:

1. Omfanget av løsningen. Dette gir en overordnet beskrivelse av hva som inngår i løsningen. Som et vedlegg til kapittelet har leverandøren inkludert et utkast til en komplett produkt backlog med estimater.
2. Prosjektgjennomføring. Dette gir en beskrivelse av metodene som benyttes, med leveranser og aktiviteter; organisering med teammedlemmer og tidsperspektiv. Beskivelsen dekker spesielt sikkerhet og testing.
3. Systemdesign beskriver hvordan løsningen fungerer og kommuniserer med omverden. Beskrivelsen omfatter mange, men ikke alle de funksjonelle aspektene. Beskrivelsen er strukturert etter 4C modellen: Context, Container, Component og Classes.
4. Administrative detaljer. Vurderinger som ikke er omfattet av andre deler av løsningsforslaget beskrives her.

# Funksjonelt omfang

Vi har valgt å gruppere funksjonaliten i grovkornede funksjonelle områder som tilsvarer det vi ser som sammenhengende funksjonalitet. Hvert område er tekstlig beskrevet med en funksjonell flyt. Hvert sted i denne funksjonelle flyten vil typisk gi opphav til en eller flere produkt backlog items. Dette kapittelet utgjør en uttømmende beskrivelse av prosjektets omfang, men detaljer under hvert punkt er utelatt i beskrivelsen.

Detaljer som ikke er beskrevet rundt hvordan forretningsregler er implementert og meldinger er utfyllt vil som en hovedregel bruke FarmaPro som kilde til hvordan de fungerer. e-Resept og reseptur har mange spesialregler og det er utenfor omfanget av løsningsbeskrivelsen å detaljere alle, men leverandøren vurderer FarmaPro som et godt svar på spørsmål om spesifikke forretningsregler.

Omfang som ikke inngår i et funksjonelt område er beskrevet som krav til tjenesten. Dette inngår også i produkt backlog.

En utkast til en fullstendig produkt backlog ligger vedlagt.

## Begrepsmodell

Følgende figur illustrerer de viktigste begrepene i DIFA og hvordan de henger sammen. I grensesnittet mellom kjedesystemene og bransjesystemet ser vi for oss at disse begrepene har engelske navn med utgangspunkt i HL7-standarder. (Se systemdesign for detaljer)

Et viktig poeng med DIFA er at man dokumenterer aksjonene som ble tatt basert på farmasøytiske varsler. Modellen må også synliggjøre de legemidlene som resepten kan utleveres for (byttegruppe mm).

Det mest uventede begrepet i modellen har vi i mangel på et bedre navn kallt "ReseptBunke". Alle resepter forskrevet av samme lege på samme dato inngår i samme "bunke". Reseptformidleren behandler dem under ett for en egenandelsperiode og DIFA må være bevisst på at en resept under ekspedering i en bunke kan skape krøll med egenandelen andre resepter i bunken. (Blåreseptforskriften § 8)

![Begrepsmodell](images/class-reseptur-konseptuell.png)

## Funksjonell flyt reseptur

1. Pasientens fastlege registerer en resept i Reseptformidleren vha sin EPJ
    * Variasjon: Lege utskriver resept til bruk i egen praksis (Forskrift om legemidler fra apotek, paragraf 5-2)
    * Variant: Legen utskriver i eget navn for å verne pasient.
2. Pasienten går til apotek og ber apotektekniker ekspedere resepten
    * Variasjon: Person med registrert fullmakt henter resept for pasient
    * Variasjon: Apotek ekspederer resept som en ordre (ingen påvirkning på systemet)
    * Variasjon: Dersom pasienten er en multidosekunde skal apotektekniker varsles om dette
3. Apotektekniker henter reseptliste fra Reseptformidleren for pasienten via sitt POS og Difa
4. Apotektekniker laster ned resepter som skal ekspederes fra Reseptformidleren
    * Variasjon: Systemet gir varsel dersom pasientens resepthistorikk indikerer at ekspederingen kan gi interaksjoner, dobbelt forskrivning, doseendring eller nytt legemiddel
    * Variasjon: Systemet formidler varsler som er registrert på legemiddelet fra legemiddelverket eller FarmaLogg
5. Apotektekniker vurderer generisk bytte og, i samråd med Farmasøyt, intervensjon
    * Variasjon: Farmasøyt bestemmer intervensjon i form av endret dose, virkestoff eller ...
6. Apotektekniker skriver ut reseptetikett og foretar teknisk kontroll
7. Farmasøyt registrerer aksjoner for advarsler farmasikontroll på apotekteknikers arbeidsstasjon eller med separat app
8. Pasient signerer og betaler for utleveringen og mottar legemidlene
9. Systemet registrerer utleveringen i Reseptformidlere og sender eventuelt refusjonskrav til HELFO

### Spesialtilfeller

* Utlevering dersom systemet er nede
* Utlevering på papirresept: Resepten som blir utlevert blir registrert av apotektekniker. Resepten kan om nødvendig registreres etter utlevering. Pasientens signatur kan registreres fra scannet dokument i stedet for signaturpad.
* Nødekspedering: Farmasøyt oppføres som rekvirent?
* Pasient ekspederer resept via nettapotek (fullstendig scenario)
* Multidose-apotek ekspederer legemidler i bruk for pasient (fullstendig scenario)
* Pasient returnerer vare for kredittering: M10 og M18 med negative beløp og antall skal generes. Egenandel på M10 for perioden må reduseres.
* Apotektekniker benytter Difa GUI for resepthåndtering (fullstendig scenario)
* Vetrinærresept - lagringstid. Sterk identifisert person kun ved A/B resept. Papirresept (system innenfor langsiktig målbilde)
* Legen sender eksepederingsanmodning til et spesifikt apotek


## Funksjonell flyt refusjon

1. Apotektekniker registerer reseptlevering for en resept med refusjonshjemmel
2. Systemet beregner korrekt refusjon
    a. Legemidler med forhåndsgodkjent refusjon (paragraf 2)
    b. Systemet slår opp egenandelfritak for pasient fra NAV
    c. Pasienten kan ikke være død
    d. Paragraf 4 (legemidler mot smittsomme sykdommer utlevert til personer som ikke er medlem i folketrygden)
    e. Pasienter uten fødselsnummer eller D-nummer, inkludert EØS borgere
    f. Individuell søknad (paragraf 3 - M3, M14, M15, M20)
    g. Medisinsk forbruksmatriell (paragraf 5) ihht pris- og produktliste fra Helsedirektoratet
    h. Prevensjonsmidler til jenter under 20 år
    i. Byttereservasjon til vare som er over trinnpris eller AUP
    j. Refusjon for preparater uten refusjonspris eller maks AUP
    k. Varsle ved AIP høyere enn akseptabelt gevinstdeling
    l. Refusjoner for tjenester ytet i apotek (inhalasjonsveiledning, LAR, i fremtiden medisinstart)
    m. Egenandeltak per resept
    n. Arbeidspris (Rundskriv 7/2008 fra Legemiddelverket (pkt. 6)) - gjelder LAR?
    o. Andre refusjonsinstanser enn NAV (jernbaneverket)
    p. Yrkesskade ?? paragraf 5-25, brystproteser
    q. H-resept - betales av helseforetakene ("men kan brukes utenfor sykehus") - separat M18 hele
    r. Verneplikt - dekke egentandel
3. Pasienten kan nå motta legemidlene og forlate apoteket
4. HELFO sender oppgjørsresultat når kravet er validert (typisk noen få minutter)
    * Unntak: Dersom HELFO avviser kravet kan apotekmedarbeider korrigere kravet og sende på nytt
    * Unntak: Dersom HELFO avviser kravet skal tjenesteleverandøren håndtere dette som en B-feil
5. Kjedens regnskapssystem henter status fra systemet på refusjonskrav og oppretter fordringer [eller er dette bedre beskrevet som faktura?]
6. HELFO sender utbetalingsmelding når betalingen er utført
7. Kjedens regnskapssystem henter status fra systemet og registrerer betaling av fordringen


## Funksjonell flyt farmasøytiske tjenester

1. Ved utlevering av legemiddel varsler systemet apotektekniker om at det kan være aktuelt å yte relevant tjeneste
   * Inhaleringsveiledning
   * Medisinstart
   * Forslag: LAR
   * Forslag: Legemiddelgjennomgang
   * Forslag: Legemiddelveiledning
   * Forslag: Vaksinering
2. Pasienten ønsker å motta tjenesten
3. Apotektekniker eller farmasøyt får opp veiledning for utførelse av tjenesten
    * Veiledningen kan være i form av et spørreskjema som fylles ut i samråd med pasient og med linker til mer omfattende dokumentasjon
4. Apotektekniker eller farmasøyt registrerer detaljer om tjenesten som ble ytet
5. Pasient signerer for tjenesten (?)
6. Apotektekniker eller farmasøyt avslutter tjenesten i systemet
7. Tjenesten blir dokumenenter i apotekets journal og overlevert til helsenorge.no
8. Systemet registrerer refusjonskrav mot HELFO
    * Refusjonskravet kan være avhengig av detaljer om tjenesten. For eksempel, fra LAR: Overvåket inntak av flytende metadon har en sats på 36,75 kr, mens buprenorfin tabletter har 98 kr

## Rapporter

Alle rapporteringsgrensesnitt må renses for personlig identifiserbar informasjon. Personopplysningsloven § 27 og pasientjournalloven § 18 forutsetter at pasienten skal ha anledning til å rette og slette personopplysninger. En konsekvens av dette er at alle kopier av personopplysningene må rettes eller slettes. Dette blir umulig i praksis dersom informasjonen har inngått i rapporteringsgrunnlag.

1. Difa overfører periodisk grunnlag der personopplysninger er fjernet til rapporteringsdatabase
2. System som bruker rapporterte data henter ned rapporter etter eget behov
    * Systemet autentiseres med en oauth2 bearer token fra kjedes AD (eller tilsvarende)
    * Systemet autoriseres basert på systemets rolle (tildelt fra kjede)
    * Systemet kan spesifisere at kun data etter et gitt tidspunkt skal returneres
3. Difa sender rapporter på email periodisk til mottakere som har behov for dette

Rapportene vil være på maskinlesbare formater.

* Folkehelseinstituttet, Rapport
* Folkehelseinstituttet, Rapport
* Mattilsynet, Rapport
* Helsetilsynet, Rapport
* Folkehelseinstituttet, Rapport
* Bransjestatistikk, Rapport
* Innsynsrapport, Rapport
* Kansellerte reseptformidleroppslag, Rapport
* Kundebetaling resept, Rapport
* Intervensjoner, Rapport
* Interaksjonsstatistikk, Rapport
* Ekspedisjonsstatistikk, Rapport
* Farmasøytiske tjenester, Rapport


## Krav til tjenesten Difa

### Pasientjournal

Både resepthistorikk og journal for farmasøytiske tjenester er underlagt Pasientjournallovens § 3.Saklig virkeområde: Loven gjelder all behandling av helseopplysninger som er nødvendig for å yte, administrere eller kvalitetssikre helsehjelp til enkeltpersoner. [Merk imidlertid Pasientjournalforskriften § 2.(Unntak for apotek)]

Disse journalene inneholder også personlig identifiserbar informasjon og vil dermed være underlagt Personvernforordningen (som erstatter ditto lov, forskrift og direktiv).

Disse to regelsettene er i stor grad overlappende og innebærer en del funksjonalitet og aktiviteter som vil inngå i prosjektplanen:

* All tilgang til journalene skal loggføres (pasientjournalloven § 16.Forbud mot urettmessig tilegnelse av helseopplysninger)
* Når en bruker slår opp pasientopplysninger skal DIFA kreve at brukeren registrerer årsaken til oppslaget samt legitimasjon for den som forespurte oppslaget om relevant
* Bruker med relevant autorisasjon skal ettergå tilgangslogg
* Pasienter må kunne få utlevert medisiner uten å bli registrert (pasientjournalloven § 17. Rett til å motsette seg behandling av helseopplysninger)
* Bruker med relevant autorisasjon kan ta ut all informasjon om en pasient på pasientens forespørsel (pasientjournalloven § 18. Informasjon og innsyn)
* Bruker med relevant autorisasjon kan rette og sperre informasjon om en pasient på pasientens forespørsel (personopplysningsloven § 27. Retting av mangelfulle personopplysninger)
* Som en del av målbilde bør DIFA avlevere informasjon om en pasient til helsenorge.no slik at pasienten kan være selvbetjent på innsyn (pasientjournalloven § 18.Informasjon og innsyn)



### Versjonshåndtering

(NB: Reseptformidleren) - foreslå endring

### Sikker og stabil drift (soner, kapasitet, opptid, DR)

### Etterlevelse av lover og regler


Dette gir opphav i følgende omfang:

* Tilgangsstyring (helsepersonelloven § 48): Apotekkjedene vil være ansvarlig for sikker og korrekt *identifisering* av bruker opp og angivelse av HPR nr. Bransjeløsning vil være ansvarlig for rettighetskontroll, spesielt opp mot Helsepersonalregisteret.
* Sikker oppbevaring av data (apotekloven, personopplysningsloven): Oppbevaring i EU (personopplysningsloven § 29). Sikkerhetstiltak ihht personopplysningsloven § 13. Opplysning vil når mulig lagres i kryptert form (OWASP ASVS 7.29). Alle datalagre vil være beskyttet med autentiseringmekanismer og systempassord vil oppbevares ihttp Normen faktaark 31. Kryptografiske nøkler vil lagres i kryptert form med en opsjon for Hardware Security Module (HSM)


### Vedlikehold og oppdatering:

* Administrator kan registrere endringer satser for refusjonsordninger som gjelder fra angitte datoer
* Systemet må kunne oppdatere lister over produkter som er godkjent for refusjon
* Ved versjonendringer skal Difa så langt det er mulig støtte bakoverkompabilitet.
* Dersom kommende endringer i Reseptformidleren krever ikke-bakoverkompatible endringer i Difa vil Difa gjøre det mulig å utføre endringer i kjedesystemene _før_ endringene trer i kraft i Reseptformidlere for å gi kjedene fleksibilitet


## Utfordrende områder

### Beregning av interaksjoner basert på meldinger fra reseptformidler

For å forbedre pasientsikkerheten samtidig som man ivaretar personvernet ville det være en fordel om Reseptformidleren kunne endres til å inkludere legemiddelkode i Reseptlista (M9.2)

Av hensyn til personvern og master data er det en fordel om Difa benytter reseptformidleren fremfor egen resepthistorikk. Av hensyn til pasientsikkerheten er det viktig å kunne se interaksjon mellom tidligere resepter og resepter som er til ekspedering.

Reseptliste-meldingen fra Reseptformidleren (M9.2) inneholder historiske reseptutleveringer. Men meldingen inneholder ikke forskrivningsinformasjonen, kun NavnFormStyrke, som er "Sammenstilling av varenavn, legemiddelform for preparatet og styrke.»

Dersom Reseptformidleren endres til å la ReseptInfo i reseptliste inkludere Forskrivning (fra ReseptDokLegemiddel) som var angitt i Resepten (M1) (eventuelt ReseptDokHandelsvare for relevante resepter), så vil Difa kunne gjøre interaksjonskontroll og varsling med Reseptlista som input (i tillegg til FarmaLogg).

Alternativt må Difa laste ned reseptene (M9.3, M9.4) eller bruke resepthistorikk for å gjøre denne beregningen.


### Oppetid ref. 99.97% - Difa, Infrastruktur, Apotekforeningen

Målepunkt per apotek eller for et kontaktpunkt.



## Vedlegg: Utkast til produkt backlog




# Prosjektgjennomføring

## Organisering og tidsplan (ref bilag 4)

### Konvertering fra FarmaPro til DIFA

#### Steg 1: Farmasøytiske tjenester

#### Steg 2: Nettapotek eller multidose

#### Steg 3a: FarmaPro bro

#### Steg 4a: FarmaPro bro + enkelte kjeder på ny POS

#### Steg 3b, 4b: Enkelte kjeder på ny POS (tilhører SSA-V)

#### Steg 5: Alle kjeder på ny POS

#### Steg 6: POS, nettapotek, multidose

### Teamsammensetning - kunde

| Rolle                  | Person             | Stikkord    |
|------------------------|--------------------|-------------|
| Prosjektkoordinator    | NN (NAF)           | Vet hva som skjer, hvem som må involveres mer og følger opp alle |
| Produkteier (e-Helse)  | Ole A. M. (Difa)   | Holder dialog med myndigheter om krav, endringer og behov i e-Helse |
| Produkteier (apotek)   | Madjid S (Difa)    | Holder dialog med apotekene om behov og leveranser |

### Teamsammensetning - leverandør

| Rolle                  | Person                    | Ansvar    |
|------------------------|---------------------------|-----------|
| Funksjonell ansvarlig  | Anders A (Espire)         | Leverandørens farmasifaglig ansvarlige       |
| Funksjonell ekspert    | NN (Espire)               | Bistår funksjonelt ansvarlig med avklaringer |
| Løsningsarkitekt       | Johannes B (Sopra Steria) | Omformer behov til utviklingsaktiviteter     |
| Senior utvikler        | NN (Sopra Steria/Espire)  | Bistår løsningsarkitekt med oppfølgning, byggmester      |
| Utvikler               | NN (Espire)               | Beskriver og implementerer testbeskriver     |
| Utvikler               | NN (Sopra Steria)         | Beskriver og implementerer testbeskriver, sikkerhetchampion     |
| Juniorutvikler         | NN (Espire)               | Løser oppgaver med naiv entusiasme   |
| Juniorutvikler         | NN (Sopra Steria)         | Løser oppgaver med naiv entusiasme   |
| Testleder              | NN (Espire?)              | Koordinerer med kjeder, e-Helse. Prosessansvarlig for testbeskriver |
| Tester                 | NN (Espire)               | Bistår utvikler med utforming av testbeskrivelser, utforskende testing |
| Tester                 | NN (Espire)               | Bistår utvikler med utforming av testbeskrivelser, utforskende testing |
| Prosjektleder          | Rikard E (Sopra Steria)   | Vet hva som skjer, hvem som må involveres mer og følger opp alle |
  


## Aktiviteter

Basert på leverandørens erfaring fra Helsedirektoratets Fastlegeprosjekt vil vi innføre sterke prosjektmessige føringer for å hyppige produksjonssettinger, høy kvalitet og enkle løsninger.

Prosjektets målsetning er *ukentlige produksjonsetting*. Det er da snakk om tekniske produksjonssettinger. Funksjonelle leveranser vil gjerne foregå med en lavere frekvens.

For å oppnå ukentlige produksjonssettinger, foreslår vi følgende prinsipper:

* Produksjonsett raskest mulig
* Velg de enkle løsningene; utsett unødvendige avgjørelser
* Endring er helt naturlig
* Automatiser det som kan automatiseres
* Fokuser på kvalitet

Disse prinsippene vil være nyttige for å prioritere hensyn underveis i prosjektet.


### Oppfølgning

* Bygg med Jenkins (eller TFS?), 80%+ enhetstestdekning
* Kildekodekontroll med Git
* Issue tracking i TFS

### Strukturerte, dialogbaserte møteplasser

* Samlokalisering med domeneeksperter (eks-ESPIRE)
* Hyppig mini-demo (med Difa AS) og demo (med apotek)
* Workshop for utarbeidelse av testbeskrivelser med utvikler, tester og funksjonell ekspert

### Testbarhet bygget inn

* Teststillas tillater både enkle og vanskelige testscenarioer og kan brukes av kjeder for å teste sine nye systemer
* Test GUI tillater utforskende testing og kan brukes av e-Helse for å sertifisere løsningen
* Alle oppgaver leveres med testbeskrivelse i Test Manager. De fleste av disse testbeskrivelsene kan benyttes av e-Helse og apotek for å verifisere

### Parprogrammering

Når man utvikler systemer som er underlagt omfattende regler og med høye kvalitetskrav har leverandøren erfaring med at parprogrammering [https://en.wikipedia.org/wiki/Pair_programming] er en effektiv måte å heve kvalitet og spre kunnskap i team.

Sett med "lean" perspektiv, motvirker parprogrammering klassiske "wastes" i prosjekter:

* Fordi grensegang mellom utvikleres ansvarsområdet blir bedre kjent alle, vil redundant funksjonalitet reduseres
* Fordi flere personer mestrer alle deler av systemet unngår man å vente på spesialistkompetanse
* Fordi effektive arbeidsmetoder spres i teamet blir alle mer effektive
* Fordi all kode blir sett av to sett med øyne reduseres antall feil
* Fordi teammedlemmene jobber tetter blir det mindre behov for avklaring i overleveringer
* Fordi teamet konsenterer seg om et mindre antall oppgaver er det færre oppgaver som er under arbeid samtidig
* Fordi et par besitter mer kompetanse enn et individ er det mindre behov for overleveringer under utviklingen av en oppgave
* Fordi alle får sjansen til å påvirke hverandre fanger vi opp kunnskapet hos alle i teamet

## Leveranser


### Sikkerhet

### Testing




# Systemdesign

Strukturen i systemdesignet er lagt opp etter 4C prinsippet fra Simon Browns bok "Software Architecture for Developer". Innfallsvinkelen beskriver systemet først fra en overordnet kontekst (første C - Context), til elementene som skal kjøre på en driftsplatform (andre C - container), til komponentene og tjenestene som realiserer funksjonaliteten (tredje C - components) til informasjonsmodellen som beskriver foretningskonseptene (siste C - classes). I vår bruk kan Context og Containers sees som uttømmende, mens Components viser prinsippene rundt referansearkitekturen med spesiell fokus på viktige aspekter rundt sikkerhet og kommunikasjon.

## Overordnede arkitekturprinsipper

### Prinsipper i behovsbeskrivelsen (fra kunde)

* Fleksibilitet. Bransjeløsningen skal utformes slik at den ikke fremstår som begrensende for endringer i apotekenes arbeidsprosesser, innhold, organisering, eierskap og infrastruktur. Time to market for ny funksjonalitet skal være kort.
* Tjenesteorientering. Bransjeløsningen skal være tjenesteorientert og sammensatt av løst koblede komponenter med åpne, standardiserte grensesnitt. Funksjonalitet og ytelsesnivå skal være hovedhensyn ved utvikling av bransjeløsningen.
* Samhandling og interoperabilitet. Bransjeløsningen skal samhandle effektivt med apotekenes egne, myndigheters og andre relevante virksomheters IT-løsninger. Bransjeløsningen skal understøtte interoperabilitet på organisatorisk, semantisk og teknisk nivå.
* Kvalitet. Teknisk og faglig kvalitet skal bygges inn i bransjeløsningens prosesser og funksjonalitet. Bransjeløsningen skal bygges på en måte som sikrer at data som eies av bransjeløsningen, data som registreres i bransjeløsningen, og data som brukes i bransjeløsningen valideres tidlig og er av høy kvalitet. 
* Brukervennlighet og effektivitet. Bransjeløsningen skal være intuitiv og lede de ansatte gjennom prosessene på en effektiv måte. Brukervennlighet innebærer at bransjeløsningen skal oppleves som enkel å bruke, gjøre apotekansatte i stand til å utnytte sin kompetanse optimalt, og ikke oppleves som en hindring i arbeidet.


## Systemlandskap og integrasjoner (4C - Context)

Dette kapittelet gir oversikt over andre systemer og aktører som systemet skal samhandle med.

### Kontekst for reseptur og refusjon

![Kontekst for reseptur og refusjon](images/context-reseptur.png)

![Kontekst under migering](images/context-reseptur-migration.png)

### Dynamisk kontekst for reseptur

![Overordnet flyt for reseptur](images/context-dynamic-reseptur.png)

![Overordnet flyt for papirreseptur](images/context-papir-reseptur.png)

### Dynamisk kontekst for refusjon

![Overordnet flyt for refusjon](images/context-dynamic-refusjon.png)

### Kontekst for farmasøytiske tjenester

* Inhalasjonsveiledning (refusjonsordning på plass)
* Medisinstart (i forespørsel)
* LAR-veiledning (refusjonsordning på vei)

![Kontekst for farmasøytiske tjenester](images/context-apotektjenester.png)

![Overordnet flyt for farmasøytiske tjenester](images/context-dynamic-apotektjenester.png)

### Kontekst for brukergrensesnittopsjonen

![Kontekst for reseptur og refusjon](images/context-gui.png)

### Integrasjonsoversikt (tabell)


## Container ("Applikasjonsarkitektur"?)

![Kjøretidsenheter i systemet (produksjon)](images/container.png)

Miljøene i denne beskrivelsen utgjør Bilag 1 for SSA-D avtalen for Difa. Ytterligere detaljer om driftsmiljøet kan finnes i Bilag 2 for SSA-D.

### Produksjonsmiljø (Teknisk drift)

![Produksjonsmiljø](images/system-prod.png)

### Platform-as-a-Service (PaaS konsept)

![Produksjonsmiljø](images/container-reference-paas.png)


### Testmiljøer

![System og utviklingstest](images/container-test.png)

![Kjedes integrasjonstest](images/container-test-kjede.png)

![e-Helse sertifisering](images/container-test-ehelse.png)

![Stress test](images/container-test-stress.png)

### Brukergrensesnitt - inkludert "toskjerms GUI" for testing

!["Toskjerm" brukergrensesnitt](images/container-gui-toskjerm.png)

### Brukergrensesnitt - opsjon: GUI som benytter kjedesystem

![Brukergrensesnitt som benytter kjedesystem](images/container-gui-kjede-API.png)

### Brukergrensesnitt - opsjon: GUI som benytter kjedesystem

![GUI for kjede uten opsjon](images/container-gui-kjede-noAPI.png)


## Component (Plattform & Teknologi)

### Reference architecture

### Sikkerhetsarkitektur

#### Tilgangskontroll

![Tilgangskontroll](images/component-sikkerhet-tilgangskontroll.png)

#### Tilgangslogg

![Tilgangslogg](images/component-sikkerhet-tilgangslogg.png)

#### Sertifikathåndtering

![Sertifikathåndtering](images/component-sikkerhet-sertifikat.png)

#### Overvåking

## Class informasjonsmodell ("Informasjonsarkitektur")

![Begrepsmodell](images/class-reseptur-logisk.png)



SWAGGER.IO for dok og test
