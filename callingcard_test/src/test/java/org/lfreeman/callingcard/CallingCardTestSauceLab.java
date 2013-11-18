package org.lfreeman.callingcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteTouchScreen;
import org.openqa.selenium.remote.RemoteWebDriver;

public class CallingCardTestSauceLab {

	private WebDriver driver;
	private static String sauceUserName;
	private static String sauceAccessKey;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		loadProperties();
		File classpathRoot = new File(System.getProperty("user.dir"));
		File appDir = new File(classpathRoot, "app");

		//compress apk and load it to saucelabs storage
		compressFile(appDir.getAbsolutePath(), "callingcard.apk", "callingcard.zip");
		uploadFile(appDir.getAbsolutePath(), "callingcard.zip", sauceUserName,
				sauceAccessKey);
	}

	@Before
	public void setUp() throws Exception {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("device", "Android");
		capabilities.setCapability(CapabilityType.BROWSER_NAME, "");
		capabilities.setCapability(CapabilityType.VERSION, "4.2");
		capabilities.setCapability(CapabilityType.PLATFORM, "MAC");
		capabilities.setCapability("app", "sauce-storage:callingcard.zip");
		capabilities.setCapability("app-package", "org.lfreeman.callingcard");
		capabilities.setCapability("app-activity", "org.lfreeman.callingcard.CalingCard");

		driver = new SwipeableWebDriver(new URL(MessageFormat.format(
				"http://{0}:{1}@ondemand.saucelabs.com:80/wd/hub", sauceUserName, sauceAccessKey)),
				capabilities);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@After
	public void tearDown() throws Exception {
		this.driver.quit();
	}

	@Test
	public void testClear() throws InterruptedException {
		assertTrue(this.isOnCallingCardScreen());
		this.typeAccesscode("17185121518");
		this.clear();
		assertTrue(this.getAccessCode().isEmpty());
		this.pressHome();
		this.launchCallingCardApp();
		assertTrue(this.getAccessCode().isEmpty());
	}

	@Test
	public void testSave() throws InterruptedException {
		String code = "17185121518";
		assertTrue(this.isOnCallingCardScreen());
		this.typeAccesscode(code);
		this.save();
		this.launchCallingCardApp();
		assertEquals(this.getAccessCode(), code);
	}

	private boolean isOnCallingCardScreen() {
		String text = "CallingCard";
		WebElement el = driver.findElement(By.name(text));
		return el.getText().equals(text);
	}

	private void save() {
		driver.findElement(By.name("Save")).click();
	}

	private void clear() {
		driver.findElement(By.name("Clear")).click();
	}

	private void typeAccesscode(String code) {
		String text = "editTextAccessNumber";
		driver.findElement(By.name(text)).sendKeys(code);
		// required if emulator is configured without hardware keyboard
		// driver.navigate().back();
	}

	private String getAccessCode() {
		String text = "editTextAccessNumber";
		return driver.findElement(By.name(text)).getText();
	}

	private void launchCallingCardApp() {
		driver.findElement(By.name("Apps")).click();
		driver.findElement(By.name("CallingCard")).click();
	}

	public void pressHome() {
		Map<String, Integer> keycode = new HashMap<>();
		keycode.put("keycode", 3);
		((JavascriptExecutor) driver).executeScript("mobile: keyevent", keycode);
	}

	private static void loadProperties() {
		Properties prop = new Properties();

		try {
			prop.load(new FileInputStream("config.properties"));
			sauceUserName = prop.getProperty("sauceUserName");
			sauceAccessKey = prop.getProperty("sauceAccessKey");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Compress apk file
	 * 
	 * @param directory
	 * @param file
	 * @param zipFileName
	 */
	private static void compressFile(String directory, String file, String zipFileName) {

		File source = new File(directory, file);
		File destination = new File(directory, zipFileName);

		byte[] buffer = new byte[1024];

		try (FileOutputStream fos = new FileOutputStream(destination);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				ZipOutputStream zos = new ZipOutputStream(bos)) {
			ZipEntry ze = new ZipEntry(file);
			zos.putNextEntry(ze);
			FileInputStream in = new FileInputStream(source);

			int len;
			while ((len = in.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}

			in.close();
			zos.closeEntry();
			System.out.println("Done");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Upload compressed apk file to Saucelabs
	 * https://saucelabs.com/appium/tutorial/3   (Appium for Android on Sauce Labs)
	 * 
	 * @param directory
	 * @param fileName
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	private static void uploadFile(String directory, String fileName, String username,
			String password) throws Exception {
		File source = new File(directory, fileName);
		String filePath = source.getAbsolutePath();
		URI uri = new URIBuilder().setScheme("http").setHost("saucelabs.com")
				.setPath(String.format("/rest/v1/storage/%s/%s", username, fileName))
				.setParameter("overwrite", "true").build();

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username,
				password));
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider).build();
		try {
			HttpPost httpPost = new HttpPost(uri);
			File file = new File(filePath);
			FileEntity entity = new FileEntity(file, ContentType.APPLICATION_OCTET_STREAM);
			httpPost.setEntity(entity);

			System.out.println("executing request" + httpPost.getRequestLine());
			CloseableHttpResponse response2 = httpclient.execute(httpPost);

			try {
				System.out.println(response2.getStatusLine());
				HttpEntity entity2 = response2.getEntity();
				EntityUtils.consume(entity2);
			} finally {
				response2.close();
			}
		} finally {
			httpclient.close();
		}
	}

	public class SwipeableWebDriver extends RemoteWebDriver implements HasTouchScreen {
		private RemoteTouchScreen touch;

		public SwipeableWebDriver(URL remoteAddress, Capabilities desiredCapabilities) {
			super(remoteAddress, desiredCapabilities);
			touch = new RemoteTouchScreen(getExecuteMethod());
		}

		public TouchScreen getTouch() {
			return touch;
		}
	}
}
