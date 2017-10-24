package no.pharmacy.web.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipFile;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import no.pharmacy.infrastructure.CryptoUtil;
import no.pharmacy.infrastructure.IOUtil;
import no.pharmacy.medication.JdbcMedicationRepository;
import no.pharmacy.medication.Medication;
import no.pharmacy.organization.JdbcHealthcareServiceRepository;
import no.pharmacy.patient.JdbcPatientRepository;
import no.pharmacy.patient.PatientRepository;
import no.pharmacy.practitioner.JdbcPractitionerRepository;
import no.pharmacy.practitioner.PractitionerRepository;
import no.pharmacy.test.FakeReseptFormidler;
import no.pharmacy.test.PharmaTestData;
import no.pharmacy.test.TestDataSource;
import no.pharmacy.web.infrastructure.auth.AuthenticationConfiguration;
import no.pharmacy.web.server.PharmaApplicationContext;
import no.pharmacy.web.server.PharmaServer;

public class PrescriptionWebTest {

    private ChromeDriver driver = new ChromeDriver();
    private static PharmaTestData testData = new PharmaTestData();
    private static FakeReseptFormidler rfMessageGateway;

    @BeforeClass
    public static void setupServer() throws Exception {
        System.setProperty("webdriver.chrome.driver", downloadChromeDriver().getPath());

        JdbcMedicationRepository medicationRepository = testData.getMedicationRepository();
        medicationRepository.refresh(JdbcMedicationRepository.SEED_URL);

        PatientRepository patientRepository = new JdbcPatientRepository(TestDataSource.patientInstance(),
                s -> new PharmaTestData().samplePatient(),
                CryptoUtil.aesKey("sndglsngl ndsglsn".getBytes()));

        PharmaApplicationContext applicationContext = new PharmaApplicationContext(medicationRepository);
        applicationContext.setPharmacistDataSource(TestDataSource.pharmacistInstance());
        applicationContext.setPatientRepository(patientRepository);
        applicationContext.setHealthcareServiceRepository(new JdbcHealthcareServiceRepository(TestDataSource.organizationsDataSource(), JdbcHealthcareServiceRepository.SEED_URL));
        rfMessageGateway = new FakeReseptFormidler(medicationRepository, patientRepository);
        applicationContext.setRfMessageGateway(rfMessageGateway);

        PractitionerRepository practitionerRepository = new JdbcPractitionerRepository(
                TestDataSource.practitionersInstance(),
                CryptoUtil.aesKey("sndglsngl ndsglsn".getBytes()));
        practitionerRepository.refresh(JdbcPractitionerRepository.SEED_URL);
        applicationContext.setPractitionerRepository(practitionerRepository);

        PharmaServer server = new PharmaServer(8081, new AuthenticationConfiguration(
                "https://difa-adfs-if.difa.ad/adfs",
                "a2846c81-5959-4187-8bec-76cb4faae2cb"), applicationContext);
        server.start();
    }

    @After
    public void stopChrome() {
        driver.close();
    }


    @Test
    public void shouldCalculateCoverage() throws Exception {
        driver.get("http://localhost:8081");
        if (driver.getTitle().equals("Sign In")) {
            driver.findElement(By.id("userNameInput")).sendKeys("DIFA\\andersboots");
            driver.findElement(By.id("passwordInput")).sendKeys("Difa2017");
            driver.findElement(By.id("passwordInput")).submit();
        }

        assertThat(driver.getTitle()).isEqualTo("Reseptekspedering | pharmacy.no");

        driver.findElement(By.linkText("BOOTS APOTEK OSLO S")).click();

        String patientNationalId = testData.unusedNationalId();
        Medication prescribedMedication = testData.medicationWithoutSubstitutes();
        rfMessageGateway.addPrescription(patientNationalId, prescribedMedication, testData.sampleDoctor());

        driver.findElement(By.id("dispenseNationalId")).sendKeys(patientNationalId);
        driver.findElement(By.id("dispenseNationalId")).submit();


        driver.findElement(By.name("selectedPrescriptions")).click();
        driver.findElement(By.id("startDispense")).click();


        WebDriverWait wait = new WebDriverWait(driver, 5L);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[value='" + prescribedMedication.getProductId() + "']")));

        driver.findElement(By.cssSelector("[value='" + prescribedMedication.getProductId() + "']")).click();
        driver.findElement(By.name("price")).sendKeys("200");
        driver.findElement(By.id("updateDispenseOrder")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("prices")));
        assertThat(driver.findElement(By.id("prices")).getText()).contains("Refusjon: 122");
    }

    public static String getOsFlavor() {
        String arch = System.getProperty("os.name").toLowerCase();
        if (arch.startsWith("windows")) {
            return "win32";
        } else {
            throw new IllegalArgumentException("Please add if-else branch for os.name " + arch);
        }
    }

    private static File downloadChromeDriver() throws IOException {
        File file = new File("target/chromedriver_" + getOsFlavor() + ".zip");

        if((System.currentTimeMillis() - file.lastModified()) > 60*60*1000) {
            URL versionUrl = new URL("https://chromedriver.storage.googleapis.com/LATEST_RELEASE");
            String version = IOUtil.toString(versionUrl.openConnection()).trim();
            URL driverUrl = new URL("https://chromedriver.storage.googleapis.com/" + version + "/" + file.getName());
            IOUtil.copy(driverUrl, file, new File("target/tmp"));
        } else {
            System.out.println(file + " already downloaded");
        }

        File chromeTargetDir = new File("target");
        try (ZipFile zipFile = new ZipFile(file)) {
            if (zipFile.size() != 1) {
                throw new IllegalArgumentException("Expected 1 zip entry in " + zipFile.getName() + " was " + zipFile.size());
            }
            return IOUtil.extract(zipFile, zipFile.entries().nextElement(), chromeTargetDir);
        }
    }

}
