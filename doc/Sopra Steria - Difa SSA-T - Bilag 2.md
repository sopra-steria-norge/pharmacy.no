    
# Innledning

Løsningsbeskrivelsen av Difa er delt inn i fire områder:

1. Omfanget av løsningen. Dette gir en overordnet beskrivelse av hva som inngår i løsningen. Som et vedlegg til kapittelet har leverandøren inkludert et utkast til en komplett produkt backlog med estimater.
2. Prosjektgjennomføring. Dette gir en beskrivelse av metodene som benyttes, med leveranser og aktiviteter; organisering med teammedlemmer og tidsperspektiv. Beskivelsen dekker spesielt sikkerhet og testing.
3. Systemdesign beskriver hvordan løsningen fungerer og kommuniserer med omverden. Beskrivelsen omfatter mange, men ikke alle de funksjonelle aspektene. Beskrivelsen er strukturert etter 4C modellen: Context, Container, Component og Classes.
4. Administrative detaljer. Vurderinger som ikke er omfattet av andre deler av løsningsforslaget beskrives her.

# Funksjonelt omfang

Vi har valgt å gruppere funksjonaliten i grovkornede funksjonelle områder som tilsvarer det vi ser som sammenhengende funksjonalitet. Hvert område er tekstlig beskrevet med en funksjonell flyt. Hvert sted i denne funksjonelle flyten vil typisk gi opphav til en eller flere produkt backlog items. Dette kapittelet utgjør en uttømmende beskrivelse av prosjektets omfang, men detaljer under hvert punkt er utelatt i beskrivelsen. Teksten skal *definerer* omfanget, men beskrivelsen av detaljene forutsetter at prosjektmedlemmene i felleskap med funksjonelle eksperter diskuterer og fastsetter forløpende i prosjektgjennomføringen.

Detaljer som ikke er beskrevet rundt hvordan forretningsregler er implementert og meldinger er utfyllt vil som en hovedregel bruke FarmaPro som kilde til hvordan de fungerer. e-Resept og reseptur har mange spesialregler og det er utenfor omfanget av løsningsbeskrivelsen å detaljere alle, men leverandøren vurderer FarmaPro som et godt svar på spørsmål om spesifikke forretningsregler.

Omfang som ikke inngår i et funksjonelt område er beskrevet som krav til tjenesten. Dette inngår også i produkt backlog.

En utkast til en fullstendig produkt backlog ligger vedlagt.

## Begrepsmodell

Følgende figur illustrerer de viktigste begrepene i DIFA og hvordan de henger sammen. I grensesnittet mellom kjedesystemene og bransjesystemet ser vi for oss at disse begrepene har engelske navn med utgangspunkt i HL7-standarder. (Se systemdesign for detaljer)

Et viktig poeng med DIFA er at man dokumenterer aksjonene som ble tatt basert på farmasøytiske varsler. Modellen må også synliggjøre de legemidlene som resepten kan utleveres for (byttegruppe mm).

Det mest uventede begrepet i modellen har vi i mangel på et bedre navn kallt "ReseptBunke". Alle resepter forskrevet av samme lege på samme dato inngår i samme "bunke". Reseptformidleren behandler dem under ett for en egenandelsperiode og DIFA må være bevisst på at en resept under ekspedering i en bunke kan skape krøll med egenandelen andre resepter i bunken. (Blåreseptforskriften § 8)

![Begrepsmodell](images/class-reseptur-konseptuell.png)

## Funksjonell flyt reseptur

TODO: Flytskjema fra Eirik

1. Pasientens fastlege registerer en resept i Reseptformidleren vha sin EPJ
    * Variasjon: Lege utskriver resept til bruk i egen praksis (Forskrift om legemidler fra apotek, paragraf 5-2)
    * Variant: Legen utskriver i eget navn for å verne pasient.
    * Variant: Legen skriver ut resept på papir. Se separat flyt.
2. Pasienten identifiserer seg på apotek og ber apotektekniker få resepten ekspedert
    * Variasjon: Pasient bruker resept-id i stedet for legitimasjon for å identifisere seg
    * Variasjon: Person med registrert fullmakt henter resept på pasientens vegne
    * Variasjon: Resepten bestilles som forsendelse over telefon eller elektronisk
    * Variasjon: Resepten hentes av institusjon eller hjemmehjelptjeneste (ekspederes som ordre)
    * Variasjon: Dersom pasienten er en multidosekunde skal apotektekniker varsles om dette. Se separat flyt for multidoselevering
3. Apotektekniker henter reseptliste fra Reseptformidleren for pasienten via sitt POS og Difa
4. Apotektekniker laster ned resepter som skal ekspederes fra Reseptformidleren
    * Variasjon: Systemet hindrer en resept som er under behandling i et annet apotek fra å ekspederes
    * Variasjon: Systemet gir apotektekniker varsel *før* nedlastning dersom pasientens resepthistorikk indikerer at ekspederingen kan gi interaksjoner, dobbelt forskrivning, doseendring eller nytt legemiddel
    * Variasjon: Systemet formidler varsler som er registrert på legemiddelet fra legemiddelverket eller FarmaLogg
5. Apotektekniker vurderer bytte til et rimeligere legemiddel i samme byttegruppe
    * Variasjon: Lege, apotek eller pasient kan reservere seg mot generisk bytte
    * Variasjon: Farmasøyt bestemmer intervensjon i form av endret dosering, vare, refusjonshjemmel eller personopplysninger
7. Apotektekniker skriver ut reseptetikett og foretar teknisk kontroll ved å lese av strekkode på pakning og etikett
    * Variant: Apotektekniker kan skrive ut avstemplingslapp eller navnelapp for senere avhenting
    * Variant: Systemet sjekker avlest QR-kode på pakning mot forfalskningsregisteret
8. Farmasøyt registrerer aksjoner for advarsler farmasikontroll på apotekteknikers arbeidsstasjon eller på separat arbeidsstasjon eller mobil enhet
9. Pasient signerer og betaler for utleveringen og mottar legemidlene
9. Systemet registrerer utleveringen i Reseptformidlere og sender eventuelt refusjonskrav til HELFO. Reseptformidleren markerer resepten som ekspedert dersom dersom det ikke gjenstår ekspederinger.

### Spesialtilfeller

* Dersom DIFA eller Reseptformidleren ikke kan nåes fra apotekets POS kan apotektekniker behandle resept som nødresept
* Utlevering på papirresept: Legemidlene på resepten som blir utlevert blir registrert av apotektekniker manuelt. Resepten kan om nødvendig registreres etter utlevering. Pasientens signatur kan registreres fra scannet dokument i stedet for signaturpad.
* Nødekspedering: Farmasøyt oppføres som rekvirent?
* Pasient ekspederer resept via nettapotek (fullstendig scenario)
* Multidose-apotek ekspederer legemidler i bruk for pasient (fullstendig scenario)
* Pasient returnerer vare for kredittering: M10 og M18 med negative beløp og antall skal generes. Egenandel på M10 for perioden må reduseres.
* Apotektekniker benytter Difa GUI for resepthåndtering (fullstendig scenario)
* Veterinærresept - lagringstid. Sterk identifisert person kun ved A/B resept. Papirresept (system innenfor langsiktig målbilde)
* Legen sender eksepederingsanmodning til et spesifikt apotek
* Anbrudd


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
    m. Egenandeltak per tremånedersperiode reseptark
    n. Arbeidspris (Rundskriv 7/2008 fra Legemiddelverket (pkt. 6)) - gjelder tilbereding av antibiotikamiksturer
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


1. Ved utlevering av legemiddel varsler systemet apotektekniker om at det kan være aktuelt å yte relevant tjeneste basert på resept eller tjenestehistorikk (NB: Er dette tillatt ifg personvernforordningen?)
   * Inhalasjonsveiledning
   * Medisinstart
   * Forslag: LAR
   * Forslag: Legemiddelgjennomgang
   * Forslag: Legemiddelveiledning
   * Forslag: Vaksinering
   * Forslag: Føflekkscanning
2. Pasienten ønsker å motta tjenesten
3. Apotektekniker eller farmasøyt får opp veiledning for utførelse av tjenesten
    * Veiledningen kan være i form av et spørreskjema som fylles ut i samråd med pasient og med linker til mer omfattende dokumentasjon
4. Apotektekniker eller farmasøyt registrerer detaljer om tjenesten som ble ytet
5. Pasient signerer for tjenesten (?)
6. Apotektekniker eller farmasøyt avslutter tjenesten i systemet
7. Tjenesten blir dokumenenter i apotekets journal og overlevert til helsenorge.no
8. Systemet registrerer refusjonskrav mot HELFO
    * Refusjonskravet kan være avhengig av detaljer om tjenesten. For eksempel, for LAR: Overvåket inntak av flytende metadon har en sats på 36,75 kr, mens buprenorfin tabletter har 98 kr. For Medisinstart: Ulik refusjonspris per oppfølgingspunkt


## Pasientjournal

Både resepthistorikk og journal for farmasøytiske tjenester er underlagt Pasientjournallovens § 3.Saklig virkeområde: "Loven gjelder all behandling av helseopplysninger som er nødvendig for å yte, administrere eller kvalitetssikre helsehjelp til enkeltpersoner." [Merk imidlertid Pasientjournalforskriften § 2.(Unntak for apotek)]

Disse journalene inneholder også personlig identifiserbar informasjon og vil dermed være underlagt Personvernforordningen (som erstatter ditto lov, forskrift og direktiv).

Disse to regelsettene er i stor grad overlappende og innebærer en del funksjonalitet og aktiviteter som vil inngå i prosjektplanen:

* All tilgang til journalene skal loggføres (pasientjournalloven § 16.Forbud mot urettmessig tilegnelse av helseopplysninger)
* Når en bruker slår opp pasientopplysninger skal DIFA kreve at brukeren registrerer årsaken til oppslaget samt legitimasjon for den som forespurte oppslaget om relevant
* Bruker med relevant autorisasjon skal ettergå tilgangslogg
    * Variant: Bruker kan søke opp kritiske hendelser som nødekspedering og intervensjon
* Pasienter må kunne få utlevert medisiner uten å bli registrert (pasientjournalloven § 17. Rett til å motsette seg behandling av helseopplysninger). MÅ DISKUTERES HVORDAN DET SKAL LØSES FUNKSJONELT.
* Bruker med relevant autorisasjon kan ta ut all informasjon om en pasient på pasientens forespørsel (pasientjournalloven § 18. Informasjon og innsyn)
* Bruker med relevant autorisasjon kan rette og sperre informasjon om en pasient på pasientens forespørsel (personopplysningsloven § 27. Retting av mangelfulle personopplysninger)
* Som en del av målbilde bør DIFA avlevere informasjon om en pasient til helsenorge.no slik at pasienten kan være selvbetjent på innsyn (pasientjournalloven § 18.Informasjon og innsyn)
* Pasientjournalen vil oppdage og forhindre at brukere forsøker å hente ut store mengder med data på kort tid ("resource governor")


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

### Sikkerhet

* Tilgangsstyring (helsepersonelloven § 48): Apotekkjedene vil være ansvarlig for sikker og korrekt *identifisering* av bruker opp og angivelse av HPR nr. Bransjeløsning vil være ansvarlig for rettighetskontroll, spesielt opp mot Helsepersonalregisteret.
* Sikker kommunikasjon med eHelse: Systemet skal signere meldinger til HELFO og RF i henhold til Rammeverk for elektronisk meldingsutveksling i helsesvesnet
    * Systemet skal behandle private nøkler til virksomhetssertifikater uten at forretningstjenester har direkte tilgang til nøklene (OWASP ASV 7.11)
    * Systemet skal tillate trygg oppdatering av virksomhetssertifikater
* Personopplysninger vil så langt det er mulig lagres i kryptert form. (OWASP ASVS 7.29). Sikker oppbevaring av data (apotekloven, personopplysningsloven): Oppbevaring i EU (personopplysningsloven § 29). Sikkerhetstiltak ihht personopplysningsloven § 13.
* Personopplysninger skal fjernes fra systemet etter 12 måneder. Vinduet for historiske data skal være mulig å endre.

* Prosjektet skal gjøre sikkerhetsrevisjon ved hver viktige leveranse, herunder skal det verifiseres:
    * Logger skal ikke inneholde personsopplysninger (OWASP ASVS 8.1)
    * Alle datalagre vil være beskyttet med autentiseringmekanismer og systempassord vil oppbevares (Normen faktaark 31 og OWASP AVSV 7.13).
    * Systemet er satt opp og konfigurert i henhold til korrekte sonemodeller


### Dokumentasjon og testbarhet

* Systemets API skal dokumenteres i Swagger UI som også kan brukes for å teste alle tjenestekall mot et testsystem
* Systemet skal leveres med et brukergrensesnitt som kan brukes til å demonstrere og verifisere oppførsel uten å være koblet mot et kjedesystem
* Testsystemet skal være satt opp med Active Directory for en fiktiv apotekkjede
* Testsystemet skal være satt opp med en statisk kopi av FarmaLogg og HPR
* Testsystemet skal leveres med en simulator for kommunikasjon med RF, HELFO og NAV



### Drift, vedlikehold og oppdateringer:

* DIFA skal kunne motta datadump-filer fra eksisterende FarmaPro instanser. Systemet skal effektivt håndtere fulleksport minst en gang i døgnet, slik at FarmaPro kan laste opp filer i en lang overgangsperiode. Eksportfilene skal være lagret kryptert der den private nøkkelen kun er kjent av DIFA-systemet.
* Administrator kan registrere endringer satser for refusjonsordninger som gjelder fra angitte datoer (alternativt: Informasjonen hentes fra et annet system)
* Systemet må kunne oppdatere lister over legemidler og produkter som er godkjent for refusjon
* SLA overvåking: Systemadministrator og systemeier skal kunne se på statistikk over alle tjenestekall per tjeneste og aggregert på tjenesteområder. Statistikken skal inneholde bruksfrekvens og responstidstatistikk. Statistikk skal kunne vises med oppløsning på månedsnivå og dagsnivå.
* Enkeltnoder i applikasjonen skal kunne fjernes og legges til uten å berøre brukerne. Applikasjonsservere må være uavhengig av sesjonstilstand.
* Nye versjoner av systemet skal kunne produksjonssettes uten å berøre brukere
    * Fatale feil ved nye versjoner skal kunne oppdages raskt og rulles tilbake med påvirkning på et minimum antall apotek
* Feilhåndtering: Systemadministrator skal varsles dersom en kritisk hendelse inntreffer i logger
    * Systemadministrator skal kunne se årsaken til vanlige problemer et sentral loggverktøy (Splunk)
* Ved nye leveranser skal det dokumenteres hvordan bakoverkompabilitet og fremoverkompabilitet er støttet. Viktige vurderinger i den forbindelse:
    * Klienten angir API versjon som en del av URL. DIFA vil støtte eldre versjoner så langt kjedene er avhengig av dem og de lar seg støtte gitt forholdene til reseptformidleren.
    * Nye leveranser kan legge til output-felter og ikke-påkrevde input-felter på et eksisterende API. Teknisk sagt skal nye leveranser av eksisterende API støtte Liskovs Subtitution Principle (LSP)
    * Endringer i API som endrer navn på felter, fjerner felter eller legger til nye påkrevde input-felter skal leveres i form av en ny API versjon.
    * Dersom kommende endringer i Reseptformidleren krever ikke-bakoverkompatible endringer i Difa vil Difa gjøre det mulig å utføre endringer i kjedesystemene _før_ endringene trer i kraft i Reseptformidlere for å gi kjedene fleksibilitet


## Utfordrende områder

### Sertifikathåndtering

### Beregning av interaksjoner basert på meldinger fra Reseptformidleren

For å forbedre pasientsikkerheten samtidig som man ivaretar personvernet ville det være en fordel om Reseptformidleren kunne endres til å inkludere legemiddelkode i Reseptlista (M9.2)

Av hensyn til personvern og master data er det en fordel om Difa benytter reseptformidleren fremfor egen resepthistorikk. Av hensyn til pasientsikkerheten er det viktig å kunne se interaksjon mellom tidligere resepter og resepter som er til ekspedering.

Reseptliste-meldingen fra Reseptformidleren (M9.2) inneholder historiske reseptutleveringer. Men meldingen inneholder ikke forskrivningsinformasjonen, kun NavnFormStyrke, som er "Sammenstilling av varenavn, legemiddelform for preparatet og styrke.»

Dersom Reseptformidleren endres til å la ReseptInfo i reseptliste inkludere Forskrivning (fra ReseptDokLegemiddel) som var angitt i Resepten (M1) (eventuelt ReseptDokHandelsvare for relevante resepter), så vil Difa kunne gjøre interaksjonskontroll og varsling med Reseptlista som input (i tillegg til FarmaLogg).

Alternativt må Difa laste ned reseptene (M9.3, M9.4) eller bruke resepthistorikk for å gjøre denne beregningen.


### Oppetid ref. 99.97% - Difa, Infrastruktur, Apotekforeningen

Oppetiden og responstid er målt fra point-of-delivery på de datasentrene leverandøren kommer til å sette opp. 

Full løsningsbeskrivelse av hvordan vi oppnår opptid, recovery point objective, recovery time objective og disaster recovery finnes i DIFA SSA-D Bilag 2.

For å oppnå forespurt tilgjengelighet på 99.97% mener leverandøren det er påkrevd å ha tre datasentre med tre separate points-of-delivery. Dette er kostnadsdrivende. Dersom det formelle kravet kan reduseres vil det være tilstrekkelig med to datasentre.


## Vedlegg: Utkast til produkt backlog



# Prosjektgjennomføring

## Organisering og tidsplan (ref bilag 4)

### Konvertering fra FarmaPro til DIFA

I konkurransegrunnlaget har kunden beskrevet en milepælsplan som starter med dagens reseptur-funksjonalitet som versjon 1.0 og inkluderer ytterligere tjenester i versjon 2.0. Leverandøren frykter at denne planen vil eksponere prosjektet for stor risiko ved første viktige leveranse. Derfor foreslår vi i stedet at prosjektet går opp leveranseapparatet med en leveranse som har lavere forretningsmessig risiko. Vi foreslår i stedet å legge inn medisinstart, legemiddelassistert rehabilitering og inhalasjonsveiledning som en prøveleveranse som beviser levedyktigheten til prosjektmetoden og plattformen. En total prosjektplan kan være som følger (numre angir cirka uketall).

Aktivitetene markert med lysegrønt inngår ikke i omfanget i SSA-T men leveres som en del av SSA-V. Det er allikevel inkludert for å gi et bedre totalbilde.   

![Leveranseoversikt](images/gantt-transisjonsplan.png)

Hvert funksjonelle omfang har en utviklingsfase med tilhørende spesifiseringsaktiviteter. Godkjenningsperioden utgjør første del av produksjonsperiodene. Leverandøren foreslår følgende funksjonelle leveranser. Prosjektet vil foreta en teknisk produksjonsetting med cirka ukentlig frekvens, men funksjonalitet vil godkjennes når et funksjonelt område er komplett.


#### Steg 1: Testbart API

Det testbare API'et vil la apotekene teste sine POS systemer mot et API i et simulert, ikke-personsensitivt miljø der Reseptformidleren og HELFO er erstattet med simulatorer. API'et vil også leveres med et enkelt GUI som vil inkludere integrasjon med kjedenes brukersystemer (Active Directory).

Ved å levere et versjon som ikke behandler personsensitiv informasjon i produksjonsmiljøet først vil vi sette opp et produksjonsklart miljø uten risiko på brudd på regelverk.


#### Steg 2: Farmasøytiske tjenester

Med farmasøytiske tjenester kan apotekene registrere medisinstart, LAR og inhalasjonsveiledning, kreve refusjon fra HELFO, følge opp refusjonskrav og oppfylle minimumskrav i pasientjournalloven og personvernforordningen. Løsningen vil inkludere et brukergrensesnitt for å registrere farmasøytiske tjenester som autentiseres med kjedes brukersystemer.

Versjonen vil inkludere viktige sikkerhetsmekanismer som vil understøtte resepturflyten: Sertifikathåndtering i kommunikasjonen mot HELFO (M18) og nødvendige sikkerhetsmekanismer for å understøtte dette (HSM).

Denne leveransen er ment til å tas i bruk av apotekene. Dette er en leveranse med lav risiko: Dersom noe ikke skal være av tilstrekkelig kvalitet kan apotekene benytte eksisterende løsninger for å registrere og kreve refusjon for relevante farmasøytiske tjenester. Det vil antageligvis være hensiktmessig å ta i bruk kun én farmasøytisk tjeneste den første måneden etter leveranse.


#### Steg 3: Resepthistorikk

Versjonen vil inkludere å sette opp datamigrering fra FarmaPro til sentralt resepthistorikk, inkludert et GUI for å gjøre oppslag i resepthistorikk. Versjonen vil inkludere en minimumsløsning for å oppfylle pasientjournalloven, spesielt tilgangslogging og pasientinnsyn. I denne versjonen vil spesielt pasientinnsyn være støtte med det minimum som loven krever.

Leveransen vil også inkludere de mest kritiske rapporteringsfunksjonene, spesielt bransjestatistikken.


#### Steg 4: Reseptur API

Denne versjonen vil utvide API'et for reseptur med integrasjon mot HELFO og Reseptformidleren og inkludere et enkelt GUI for demo, test og akseptanse av løsningen.

Denne versjonen fullfører versjon 1.0 av omfanget i løsningsbeskrivelsen, samt farmasøytiske tjenester.


#### Steg 5: Reseptur fullstendig (avtalefestet i SSA-V-avtalen)

Denne leveransen vil inkludere rapporter som ikke allerede er levert, samt forbedringer for å etterleve pasientjournalloven. Spesielt foreslår leverandøren at pasientinnsyn ivaretas gjennom et samarbeid med helsenorge.no.

#### Steg 6: Nettapotek (avtalefestet i SSA-V-avtalen)

#### Steg 7: Multidoseapotek (avtalefestet i SSA-V-avtalen)


### Teamsammensetning - kunde

| Rolle                  | Person (eks)       | Stikkord    |
|------------------------|--------------------|-------------|
| Prosjektkoordinator    | NN (NAF)           | Vet hva som skjer, hvem som må involveres mer og følger opp alle |
| Produkteier (e-Helse)  | Ole A. M. (Difa)   | Holder dialog med myndigheter om krav, endringer og behov i e-Helse |
| Produkteier (apotek)   | Madjid S (Difa)    | Holder dialog med apotekene om behov og leveranser |

### Teamsammensetning - leverandør

| Rolle                  | Person (eks)              | Ansvar    |
|------------------------|---------------------------|-----------|
| Funksjonell ansvarlig  | Anders A (Espire)         | Leverandørens farmasifaglig ansvarlige       |
| Funksjonell ekspert    | NN (Espire)               | Bistår funksjonelt ansvarlig med avklaringer |
| Løsningsarkitekt       | Johannes B (Sopra Steria) | Omformer behov til utviklingsaktiviteter     |
| Senior utvikler        | NN (Sopra Steria/Espire)  | Bistår løsningsarkitekt med oppfølgning, byggmester      |
| Utvikler               | NN (Espire)               | Beskriver og implementerer testbeskrivelser     |
| Utvikler               | NN (Sopra Steria)         | Beskriver og implementerer testbeskrivelser, sikkerhetchampion     |
| Juniorutvikler         | NN (Espire)               | Løser oppgaver med naiv entusiasme   |
| Juniorutvikler         | NN (Sopra Steria)         | Løser oppgaver med naiv entusiasme   |
| Testleder              | NN (Espire?)              | Koordinerer med kjeder, e-Helse. Prosessansvarlig for testbeskrivelser |
| Tester                 | NN (Espire)               | Bistår utvikler med utforming av testbeskrivelser, utforskende testing |
| Tester                 | NN (Espire)               | Bistår utvikler med utforming av testbeskrivelser, utforskende testing |
| Prosjektleder          | Rikard E (Sopra Steria)   | Vet hva som skjer, hvem som må involveres mer og følger opp alle |

Teamet vil inkludere noen deltidsroller ("hatter") som enkeltpersoner vil utføre i tillegg til sine primære roller:

* Security Champion: Engasjert i personvern- og sikkerhetsmessige problemstillinger og inspirere teamet rundt OWASP, statisk kodeanalyse, dynamisk kodeanalyse osv.
* Byggmester: Engasjert i å gjøre bygg, utrulling og overvåking smidigere og tryggere


## Aktiviteter

Basert på leverandørens erfaring fra Helsedirektoratets Fastlegeprosjekt vil vi innføre sterke prosjektmessige føringer for å hyppige produksjonssettinger, høy kvalitet og enkle løsninger.

Prosjektets målsetning er *ukentlige produksjonsetting*. Det er da snakk om tekniske produksjonssettinger. Funksjonelle leveranser vil gjerne foregå med en lavere frekvens.

For å oppnå ukentlige produksjonssettinger, foreslår vi følgende prinsipper:

* Produksjonsett raskest mulig
* Velg de enkle løsningene; utsett unødvendige avgjørelser
* Endring er helt naturlig
* Automatiser det som kan automatiseres
* Fokuser på kvalitet

Disse prinsippene vil være nyttige for å prioritere hensyn rundt planlegging, krav, arkitektur, utvikling, testing og leveranser underveis i prosjektet.


### Prosjektets eksterne takt

Vi forventer at det funksjonelle omfanget til prosjektet kommer til å være omforent ved prosjektets start. De mest sentrale oppgavene kommer også til å være de facto detaljspesifisert gjennom grensesnittbeskrivelser fra eHelse og lovmessige bestemmelser. Prosjektet vil derfor starte å ferdigstille funksjonalitet i et miljø som gradvis vil bli mer produksjonsklart med ukentlig frekvens.

Hver uke vil prosjektet avholde en times minidemo der Apotekforeningen og alle prosjektmedlemmer forventes å være til stede. Hver måned, med start etter én måned, vil det tilsvarende avholdes en mer formell demo der apotekkjedene også er invitert til å stille.

Første akseptansetest av delleveranse 0.1 vil forventes klar etter 3 måneder. Denne løsningen vil ikke inneholde reele data, men vil kunne benyttes av apotekene til å teste sin integrasjon. Delleveranse 0.2, 0.3 og 1.0 vil forventes med litt høyere frekvens enn kvartalsvis. Funksjonalitet som inngår i leveransene vil produksjonssettes fortløpende med cirka ukentlig frekvens. Uferdig funksjonalitet vil være skrudd av i disse produksjonssettingene.

Akseptansetest vil foregå med representanter for kjedene (?) og direktoratet for e-Helse.

Oppsummert:
* Ukentlige interne demoer
* Ukentlige produksjonssettinger
* Månedlige demoer med kjedene
* Leveranser med akseptansetesting kvartalsvis eller hyppigere


### Prosjektets interne takt

Prosjektets aktiviteter vil være strukturert etter en sortert product backlog. Backloggen vil foreligge i en stabil versjon ved avtaleinngåelse. Leverandørens funksjonell ansvarlig, leverandørens arkitekt og kundens produkteiere vil prioritere og endringshåndtere backloggen. Det forventes kun uvesentlige endringer i omfanget av SSA-T avtalen.

Oppgavene på backlogg skal være fortrinnsvis være på cirka 1-2 utvikleruker i omfang.

Utviklingsteamet vil levere fra backlogg fortløpende. Utviklerne på teamet vil jobbe parvis og vil under stand-up møtet om morgenen eventuelt plukke en ny oppgave blant de høyest sorterte oppgavene på backlogg. Utviklerparet vil sette av tid med funksjonell ekspert og tester på teamet for å utforme en testbeskrivelse for oppgaven. Testbeskrivelsen skal vurderes av kunden ihht til bestemmelsene i Bilag 6.

Utviklerne vil fortløpende utvikle på oppgaven. Hver endring vil automatisk testes i et verktøy for kontinuerlig bygg hver endring vil settes i automatisk drift på et testmiljø som etableres i løpet av første måned. I løpet av første leveranse vil leverandøren etablere automatiske statiske kontroller av koden.

Når utviklerne har fullført oppgaven vil de kontrollere den mot testbeskrivelsen i samarbeid med en tester. Funksjonaliteten vil overleveres på neste ukentlige demomøte med kunden. Kunden skal vurdere korrekthet ihht til bestemmelsene i Bilag 6.

Oppsummert:
* Prosjektets omfang er definert i form av en product backlogg
* Kundens produkteiere, leverandørens funksjonelt ansvarlige og leverandørens arkitekt forvalter produkt backlogg
* Utviklere jobber i par
* Utviklere velger oppgaver fra øverste del av product backlogg
* Utviklere, tester og funksjonell ekspert utarbeider testbeskrivelse når en oppgave påbegynnes
* Alle endringer testes og driftsettes automatisk
* 2-4 ferdige oppgaver overleveres til kunden ukentlig

### Produkt backlogg elementer

Produkt backlogg vil forvaltes i TFS.

Oppgavene på produkt backlogg skal dekke alle aktiviteter som skal til for at oppgaven skal kunne medføre at funksjonalitet kan benyttes av kjedesystem eller apotekbruker i neste leveranse. Avhengig av oppgaven kan det innebære:

* Utarbeide eller oppdatere testbeskrivelser som kan benyttes av kjedene eller direktoratet for eHelse
* Oppdater testbrukergrensesnitt til å kunne teste funksjonaliteten
* Oppdater testsimulator for å kunne teste funksjonaliteten
* Oppdatere API dokumentasjon i Swagger
* Utvikle automatiske tester for funksjonaliteten med minst 80% testdekning
* Utvikle selve funksjonalitene
* Utvikle relevant unntakshåndtering for funksjonaliteten
* Utvikle databasescript for å understøtte eventuell nye lagringsbehov
* Utvikle integrasjon med eksterne parter for å understøtte funksjonaliteten


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


### Leverandørens foreståelse av arkitekturprinsippene

* Fleksibilitet: DIFA er et API som eksponeres til apotekene. API-et vi ha som hovedprinsipp at det er få påkrevde tjenestekall og disse behandle komplett informasjon. I tillegg vil DIFA leverer hjelpetjenester som kjedesystemene kan velge å kalle der det passer for arbeidsflyten de implementerer. Fleksibiliteten er imidlertid begrenset til mulighetsrommet i eResept
* Tjenesteorientering: DIFA vil definere et grensesnitt dokumentert med Swagger UI. Alle tjenester vil være underlagt målinger av kallfrekvens og responstid.
* Samhandling. DIFA vil legge til grunn internasjonale standarder for navngiving og struktur i API, spesielt HL7 standarder. DIFA vil ta førerrollen med å definere stabile meldingsformater som bransjen kan forholde seg til og som understøtter myndighetskrav
* Kvalitet. DIFA vil validere refusjoner og aksjoner på farmasøytiske varsler. Avviste refusjonskrav vil registreres som B-feil i DIFA forvaltning.
* Brukervennlighet og effektivitet. DIFA skal ikke investere i et avansert brukergrensesnitt. I stedet vil kravet om fleksbilitet understøtte kjedenes evne til å levere brukervennlige og effektive løsninger.


## Systemlandskap og integrasjoner (4C - Context)

Dette kapittelet gir oversikt over andre systemer og aktører som systemet skal samhandle med. Det er ment for å skape forståelse for hvilke prosesser systemet inngår i og understøtter.

### Kontekst for reseptur og refusjon

I målbildet av systemet formidler DIFA kommunikasjonen mellom kjedens systemer (POS, ERP, Nettapotek, Multidose) og systemene som inngår i det norske helsevesenet (Reseptformidlere, HELFO, NAV, HPR, Farmalogg).

Figuren tar kun høyde for normalflyten for reseptur og for eksempel ekspederingsanmodning er utelatt.

![Kontekst for reseptur og refusjon](images/context-reseptur.png)

![Kontekst ved bruk av GUI-opsjon](images/context-gui.png)

Noen kjeder kan ønske å benytte brukergrensesnitt for reseptur levert som en del av DIFA. I dette tilfelle vil apotektekniker benytte dette systemet for å forberede resepten og POS for å motta betaling og signatur for resepten. POS vil varsle DIFA om at betalingen er komplett slik at DIFA kan formidle utleveringen til Reseptformidleren og sende Refusjonskrav til HELFO.

### Dynamisk kontekst for reseptur

Disse er antageligvis utdaterte.

![Overordnet flyt for reseptur](images/context-dynamic-reseptur.png)

![Overordnet flyt for papirreseptur](images/context-papir-reseptur.png)

### Dynamisk kontekst for refusjon

![Overordnet flyt for refusjon](images/context-dynamic-refusjon.png)

### Kontekst for farmasøytiske tjenester

DIFA skal veileide, informere, registrere og håndtere refusjon for farmasøytiske tjenester levert i apotek. De mest aktuelle tjenestene er inhalasjonsveiledning og medisinstart, ettersom det er tjenester som allerede leveres i dag. Legemiddel-assistert rehabilitering (LAR) er også en aktuell tjeneste ettersom den har fått refusjonsordning.

Kjeden kan velge om farmasøyt skal registrere tjenesten i et brukergrensesnitt i DIFA eller i kjedens eget system. Vi anser at det er lite interaksjon med kjedens andre aktiviteter og at det derfor er mest aktuelt å benytte et brukergrensesnitt i DIFA.

![Kontekst for farmasøytiske tjenester](images/context-apotektjenester.png)

![Overordnet flyt for farmasøytiske tjenester (brukergrensesnitt i DIFA)](images/context-dynamic-apotektjenester.png)


### Integrasjonsoversikt

| Hvem                 | Hva                  | Hvordan                                     |
|----------------------|----------------------|---------------------------------------------|
| POS -> DIFA          | Resepturekspedering  | JSON POST basert API autentisert med oauth2 |
| Nettapotek -> DIFA   | Resepturekspedering  | JSON POST basert API autentisert med oauth2 |
| Multidose  -> DIFA   | Resepturekspedering  | JSON POST basert API autentisert med oauth2 |
| ERP  -> DIFA         | Refusjonsstatus      | JSON GET basert API autentisert med oauth2 på applikasjonsnivå |
| Reseptformidler      | Resepturekspedering  | M9.1, M9.2, M9.3, M9.4 med ebXML over SOAP  |
| HELFO                | Refusjonskrav        | M18 ebXML over SMTP                         |
| HELFO -> DIFA        | Refusjonsstatus      | M22, M23 ebXML over SMTP                    |
| eHelse               | Egenandelstatus      | DIFA henter status for egenandelfritak      |
| FarmaLogg            | Legemidler           | DIFA henter. Løsningsforslag: HTTP GET XML  |
| Helsedir             | Helsepersonell       | DIFA henter HPR personregister.hl7          |
| helsenorge.no (fremtidig) | Pasientjournal  | DIFA leverer. To be decided                 |

Grensesnittene mot Reseptformidleren og HELFO er beskrevet på https://ehelse.no/e-resept-kjernejournal-og-helsenorgeno/e-resept, samt https://ehelse.no/ebxml-rammeverk-his-10372011

Grensesnitt mot Helsepersonellregister er beskrevet på https://www.nhn.no/hjelp-og-brukerstotte/personregisteret/andreressurser/Introduksjon-til-integrasjon-med-registrene.pdf


## Applikasjonsarkitektur

Applikasjonsarkitekturen samsvarer med "Container" nivået i arkitekturmodellen. Her viser vi alle komponenter som skal settes i drift og tjenestene som kjører på disse komponentene. Applikasjonsarkitekturen har to innfallsvinkler: For det første illustrerer den de programvarekomponentene som må utvikles eller kjøpes inn. For det andre viser den hvilken interaksjon systemet vil ha med driftsplattformen og setter rammene som SSA-D.

![Kjøretidsenheter i systemet (produksjon)](images/container.png)

### Systemoversikt

Denne tabellen viser en oversikt over de komponentene som har selvstendig liv i produksjonsmiljøet. Oversikten vil justeres og oppdateres under behov under utvikling av systemet.

Vi har beskrevet systemet som en Java-plattform, men er fortsatt åpen for at den kan kjøre på .NET core i stedet for. Vi vurderer også SQL Server i stedet for PostgreSQL.

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

Miljøene i denne beskrivelsen utgjør Bilag 3 for SSA-D avtalen for Difa. Ytterligere detaljer om driftsmiljøet kan finnes i Bilag 2 for SSA-D.

### Produksjonsmiljø (Teknisk drift)

![Produksjonsmiljø](images/system-prod.png)

### Platform-as-a-Service (PaaS konsept)

Difa vil leveres på OpenShift - en docker-basert cloud infrastruktur. OpenShift håndterer laget mellom virtuelle maskiner (VMWare) og Docker-containere. Utviklingsprosjektet vil levere docker images. Applikasjonsdrift (DevOps) vil være ansvarlig for å oppgradere og skalere docker-containere på hardware som er levert innen driftsavtalen. Ytterligere konsekvenser av driftsplattformen er beskrevet i SSA-D-avtalen.

![Produksjonsmiljø](images/container-reference-paas.png)

### Testmiljøer

For å understøtte de forskjellige testaktivitetene vil det være behov for et stort antall testmiljøer. De fleste av disse miljøene vil ha minimale ytelsesbehov og vil leve samme på samme VMWare-instans (unntatt stresstestmiljøet). Utviklingsprosjektet kan sette opp nye testmiljøer etter behov innenfor den infrastrukturen som er levert i forbindelse med prosjektet.

![System og utviklingstest](images/container-test.png)

![Kjedes integrasjonstest](images/container-test-kjede.png)

![e-Helse sertifisering](images/container-test-ehelse.png)

![Stress test](images/container-test-stress.png)


## Component (Plattform & Teknologi)

Dette kapittelet viser hvordan systemet er sett for seg implementert. Kapittelet er delt i to deler: Først beskriver vi den sentrale funksjonaliteten knyttet til reseptur. Så beskriver vi generelle gjennomgripende implementasjon av sikkerhet og tilgangskontroll.

Funksjonaliteten knyttet til reseptur er beskrivet på tre måter: Den normale bruken der kjedens POS system eksponerer funksjonalitet gjennom DIFA API, brukergrensesnittopsjonen der resepturgrensesnittet i DIFA leser beholdning fra kjedens varesystem, og i brukergrensesnittet for test og demonstrasjon. Testbrukergrensesnittet vil fungere tilsvarende som brukergrensesnittopsjonen, men uten integrasjon med kjedens varesystem.

Vi foreslår at Farmasøyt benytter DIFA GUI til å foreta farmasøytkontroll. DIFA vil implementere et høy og samtidig pragmatisk sikkerhetsnivå for farmasøytkontroll.

### Grensegang mellom kjedesystem og bransjesystem

![Grensegang mellom kjedene og DIFA](images/container-gui-kjede-pos.png)

### Brukergrensesnitt - opsjon: GUI som benytter kjedesystem

![Brukergrensesnitt som benytter kjedesystem](images/container-gui-kjede-API.png)

### Brukergrensesnitt: Inkludert "toskjerms GUI"

![GUI for kjede uten opsjon](images/container-gui-kjede-noAPI.png)



### Reference architecture

### Sikkerhetsarkitektur

#### Tilgangskontroll

DIFA vil ikke selv ha en brukerdatabase, men vil i stedet lene seg på kjedenes brukersystemer. Vi vil integrere med disse via en standard oauth2 flyt. Der DIFA benyttes som et API bak kjedesystemet vil kjedesystemet utstede en JWT (JSON web token) og sender det som en Bearer token. Denne token er signert av kjedens Active Directory (eller tilsvarende) og vil inneholde HPR-nr, hvilke HER-id brukeren er autorisert for og applikasjonsroller. DIFA vil kontrollere brukerens helsepersonalautorisasjon mot HPR basert på brukerens HPR-nr.

Når brukeren benytter DIFAs GUI vil en oauth2 code flow mot kjedens Active Directory (eller tilsvarende) brukes for å overføre JWT til DIFA.

Kjedene er ansvarlig for å oppfylle passordkravene i Normen.

Ikke vist: For enkelte operasjoner vil DIFA kreve et høyere sikkerhetsnivå fra Active Directory. Avhengig av kjedens oppsett kan dette innebære en 2-faktor autentisering med SMS-kode eller på annet vis.

![Tilgangskontroll (API)](images/component-sikkerhet-tilgangskontroll-api.png)

![Tilgangskontroll (GUI)](images/component-sikkerhet-tilgangskontroll-gui.png)

#### Tilgangslogg

For å oppfylle kravet i personvernforordningen og pasientjournalloven vil DIFA logge alle operasjoner der en bruker aksesserer pasientinformasjon.

![Tilgangslogg](images/component-sikkerhet-tilgangslogg.png)

#### Sertifikathåndtering

Dersom DIFA ikke skal eksponere kravet om ebXML-signering til kjedene, så må DIFA oppbevare virksomhetssertifikater på vegne av kjedene. Skissen viser hvordan sertifikatene administeres og brukes.

![Sertifikathåndtering](images/component-sikkerhet-sertifikat.png)

#### Overvåking

## Informasjonsarkitektur

Følgende modell viser sentrale begreper i reseptur og hvordan de henger sammen.

![Begrepsmodell](images/class-reseptur-logisk.png)

Her vil det komme en tilsvarende modell for farmasøytiske tjenester.

