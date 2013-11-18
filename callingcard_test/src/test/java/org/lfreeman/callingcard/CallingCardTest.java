package org.lfreeman.callingcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
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

public class CallingCardTest {

	private WebDriver driver;

	@Before
	public void setUp() throws Exception {
		File classpathRoot = new File(System.getProperty("user.dir"));
		File appDir = new File(classpathRoot, "app");
		File app = new File(appDir, "callingcard.apk");
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("device", "Android");
		capabilities.setCapability(CapabilityType.BROWSER_NAME, "");
		capabilities.setCapability(CapabilityType.VERSION, "4.2");
		capabilities.setCapability(CapabilityType.PLATFORM, "MAC");
		capabilities.setCapability("app", app.getAbsolutePath());
		capabilities.setCapability("app-package", "org.lfreeman.callingcard");
		capabilities.setCapability("app-activity", "org.lfreeman.callingcard.CalingCard");
		this.driver = new SwipeableWebDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
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
