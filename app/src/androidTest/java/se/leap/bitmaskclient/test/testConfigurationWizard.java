package se.leap.bitmaskclient.test;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.test.*;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.*;

import com.robotium.solo.*;

import java.io.*;

import se.leap.bitmaskclient.*;

public class testConfigurationWizard extends ActivityInstrumentationTestCase2<ConfigurationWizard> {

    private Solo solo;
    private static int added_providers;

    public testConfigurationWizard() {
        super(ConfigurationWizard.class);
    }

    public testConfigurationWizard(Solo solo) {
        super(ConfigurationWizard.class);
        this.solo = solo;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
        ConnectionManager.setMobileDataEnabled(true, solo.getCurrentActivity().getApplicationContext());
    }

    @Override
    protected void tearDown() throws Exception {

    }

    public void testListProviders() {
        assertEquals(solo.getCurrentViews(ListView.class).size(), 1);

        assertEquals("Number of available providers differ", predefinedProviders() + added_providers, shownProviders());
    }

    private int shownProviders() {
        return solo.getCurrentViews(ListView.class).get(0).getCount();
    }

    private int predefinedProviders() {
        int predefined_providers = 0;
        try {
            predefined_providers = solo.getCurrentActivity().getAssets().list("urls").length;
        } catch (IOException e) {
            e.printStackTrace();
            return predefined_providers;
        }

        return predefined_providers;
    }

    public void testSelectProvider() {
        selectProvider("demo.bitmask.net");
    }

    private void selectProvider(String provider) {
        solo.clickOnText(provider);
        waitForProviderDetails();
    }

    private void waitForProviderDetails() {
        String text = solo.getString(R.string.provider_details_fragment_title);
        assertTrue("Provider details dialog did not appear", solo.waitForText(text));
    }

    public void testAddNewProvider() {
        addProvider("calyx.net");
    }

    private void addProvider(String url) {
        boolean is_new_provider = !solo.searchText(url);
        if (is_new_provider)
            added_providers = added_providers + 1;
        solo.clickOnActionBarItem(R.id.new_provider);
        solo.enterText(0, url);
        solo.clickOnCheckBox(0);
        solo.clickOnText(solo.getString(R.string.save));
        waitForProviderDetails();
        solo.goBack();
    }

    public void testShowAbout() {
        showAbout();
    }

    private void showAbout() {
        String text = solo.getString(R.string.about);
        solo.clickOnMenuItem(text);
        assertTrue("Provider details dialog did not appear", solo.waitForActivity(AboutActivity.class));
    }

    protected void toDashboardAnonymously(String provider) {
        selectProvider(provider);
        useAnonymously();
    }

    private void useAnonymously() {
        String text = solo.getString(R.string.use_anonymously_button);
        clickAndWaitForDashboard(text);
    }

    private void clickAndWaitForDashboard(String click_text) {
        solo.clickOnText(click_text);
        assertTrue(solo.waitForActivity(Dashboard.class, 5000));
    }

    protected void toDashboardRegistered(String provider) {
        selectProvider(provider);
        useRegistered();
    }

    private void useRegistered() {
        String text = solo.getString(R.string.signup_or_login_button);
        clickAndWaitForDashboard(text);
        login();
    }

    private void login() {
        long milliseconds_to_log_in = 40 * 1000;
        logIn("parmegvtest10", "holahola2");
        solo.waitForDialogToClose(milliseconds_to_log_in);
        assertSuccessfulLogin();
    }

    private void logIn(String username, String password) {
        solo.clickOnActionBarItem(R.id.login_button);
        solo.enterText(0, username);
        solo.enterText(1, password);
        solo.clickOnText("Log In");
        solo.waitForDialogToClose();
    }

    private void assertSuccessfulLogin() {
        String message = solo.getString(R.string.succesful_authentication_message);
        assertTrue(solo.waitForText(message));
    }

    private void connectAndComeBack() {
        solo.clickOnView(solo.getView(R.id.eipSwitch));
        if(!solo.waitForText(solo.getString(R.string.eip_state_connected)))
            fail();

        solo.clickOnView(solo.getView(R.id.eipSwitch));
        if(!solo.waitForText(solo.getString(R.string.eip_state_not_connected)))
            fail();

        solo.clickOnMenuItem(solo.getString(R.string.switch_provider_menu_option));
    }
}
