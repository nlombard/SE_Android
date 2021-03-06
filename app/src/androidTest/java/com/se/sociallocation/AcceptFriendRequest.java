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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AcceptFriendRequest {

    @Rule
    public ActivityTestRule<SplashActivity> mActivityTestRule = new ActivityTestRule<>(SplashActivity.class);

    @Before
    public void checkLogin() {
        //If signed in, log out to make sure you sign in to the proper user for these series of tests
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

            ViewInteraction appCompatTextView = onView(
                    allOf(withId(R.id.title), withText("Log Out"), isDisplayed()));
            appCompatTextView.perform(click());
        }

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
//            /*
            ViewInteraction appCompatAutoCompleteTextView = onView(
                    withId(R.id.email));
            appCompatAutoCompleteTextView.perform(scrollTo(), click());

            ViewInteraction appCompatAutoCompleteTextView2 = onView(
                    withId(R.id.email));
            appCompatAutoCompleteTextView2.perform(scrollTo(), click());

            ViewInteraction appCompatAutoCompleteTextView3 = onView(
                    withId(R.id.email));
            appCompatAutoCompleteTextView3.perform(scrollTo(), replaceText("kt@nd.edu"), closeSoftKeyboard());

            ViewInteraction appCompatEditText = onView(
                    withId(R.id.password));
            appCompatEditText.perform(scrollTo(), replaceText("hello1"), closeSoftKeyboard());

            ViewInteraction appCompatButton = onView(
                    allOf(withId(R.id.email_sign_in_button), withText("Sign in"),
                            withParent(allOf(withId(R.id.email_login_form),
                                    withParent(withId(R.id.login_form))))));
            appCompatButton.perform(scrollTo(), click());

            allowPermissionsIfNeeded();
            //*/
        }
    }

    @Test
    public void acceptFriendRequest() {
        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction appCompatCheckedTextView = onView(
                allOf(withId(R.id.design_menu_item_text), withText("Friend Requests"), isDisplayed()));
        appCompatCheckedTextView.perform(click());

        //Long Click
        ViewInteraction appCompatTextView3 = onView(
                allOf(withId(android.R.id.text1), withText("Maverick"),
                        childAtPosition(
                                allOf(withId(R.id.friend_requestsview),
                                        withParent(withId(R.id.content_friend_requests))),
                                0),
                        isDisplayed()));
        appCompatTextView3.perform(longClick());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(android.R.id.title), withText("Add Friend"), isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction appCompatCheckedTextView2 = onView(
                allOf(withId(R.id.design_menu_item_text), withText("Map"), isDisplayed()));
        appCompatCheckedTextView2.perform(click());

        ViewInteraction appCompatImageButton3 = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton3.perform(click());

        ViewInteraction appCompatCheckedTextView3 = onView(
                allOf(withId(R.id.design_menu_item_text), withText("Friends"), isDisplayed()));
        appCompatCheckedTextView3.perform(click());

        ViewInteraction appCompatTextView2 = onView(
                allOf(withId(android.R.id.text1), withText("Maverick"),
                        childAtPosition(
                                allOf(withId(R.id.friend_listview),
                                        withParent(withId(R.id.content_friend_list))),
                                0),
                        isDisplayed()));
        appCompatTextView2.perform(click());

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
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
