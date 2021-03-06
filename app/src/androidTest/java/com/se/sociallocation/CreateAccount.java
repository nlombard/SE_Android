package com.se.sociallocation;


import android.os.Build;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.test.suitebuilder.annotation.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CreateAccount {

    @Rule
    public ActivityTestRule<SplashActivity> mActivityTestRule = new ActivityTestRule<>(SplashActivity.class);

    @Before
    public void checkLogin() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

            ViewInteraction appCompatTextView = onView(
                    allOf(withId(R.id.title), withText("Log Out"), isDisplayed()));
            appCompatTextView.perform(click());
        }
    }

    @Test
    public void createAccount() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.create_account_button), withText("Sign Up"),
                        withParent(allOf(withId(R.id.email_login_form),
                                withParent(withId(R.id.login_form))))));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction appCompatAutoCompleteTextView = onView(
                withId(R.id.email));
        appCompatAutoCompleteTextView.perform(scrollTo(), replaceText("izzy@nd.edu"), closeSoftKeyboard());

        ViewInteraction appCompatAutoCompleteTextView2 = onView(
                withId(R.id.username));
        appCompatAutoCompleteTextView2.perform(scrollTo(), replaceText("Izzy"), closeSoftKeyboard());

        ViewInteraction appCompatEditText = onView(
                withId(R.id.password));
        appCompatEditText.perform(scrollTo(), replaceText("hello1"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                withId(R.id.confirm_password));
        appCompatEditText2.perform(scrollTo(), replaceText("hello1"), closeSoftKeyboard());

        ViewInteraction appCompatEditText3 = onView(
                withId(R.id.confirm_password));
        appCompatEditText3.perform(scrollTo(), replaceText("hello1"), closeSoftKeyboard());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.email_sign_up_button), withText("Sign Up"),
                        withParent(allOf(withId(R.id.email_login_form),
                                withParent(withId(R.id.login_form))))));
        appCompatButton2.perform(scrollTo(), click());

        allowPermissionsIfNeeded();
    }

    private void allowPermissionsIfNeeded() {
        if(Build.VERSION.SDK_INT >= 23) {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
//            /*
            device.waitForWindowUpdate(null, 1000);
            UiObject allowPermissions = device.findObject(new UiSelector().text("Allow"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    e.printStackTrace();
                }
            }
            //*/
        }
    }
}
