package com.niharika.android.groupexpensetracker;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.INTERNET");

    @Test
    public void mainActivityTest() {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editTextEmail),
                        childAtPosition(
                                allOf(withId(R.id.credential_layout),
                                        childAtPosition(
                                                withId(R.id.topLL),
                                                0)),
                                1)));
        appCompatEditText.perform(scrollTo(), replaceText("niharika2@gm"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.link), withText("Login"),
                        childAtPosition(
                                allOf(withId(R.id.other_login_layout),
                                        childAtPosition(
                                                withId(R.id.credential_layout),
                                                4)),
                                0)));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.link), withText("Register"),
                        childAtPosition(
                                allOf(withId(R.id.other_login_layout),
                                        childAtPosition(
                                                withId(R.id.credential_layout),
                                                4)),
                                0)));
        appCompatButton2.perform(scrollTo(), click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.link), withText("Login"),
                        childAtPosition(
                                allOf(withId(R.id.other_login_layout),
                                        childAtPosition(
                                                withId(R.id.credential_layout),
                                                4)),
                                0)));
        appCompatButton3.perform(scrollTo(), click());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.link), withText("Register"),
                        childAtPosition(
                                allOf(withId(R.id.other_login_layout),
                                        childAtPosition(
                                                withId(R.id.credential_layout),
                                                4)),
                                0)));
        appCompatButton4.perform(scrollTo(), click());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.link), withText("Login"),
                        childAtPosition(
                                allOf(withId(R.id.other_login_layout),
                                        childAtPosition(
                                                withId(R.id.credential_layout),
                                                4)),
                                0)));
        appCompatButton5.perform(scrollTo(), click());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.editTextEmail), withText("niharika2@gm"),
                        childAtPosition(
                                allOf(withId(R.id.credential_layout),
                                        childAtPosition(
                                                withId(R.id.topLL),
                                                0)),
                                1)));
        appCompatEditText2.perform(scrollTo(), click());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.editTextEmail), withText("niharika2@gm"),
                        childAtPosition(
                                allOf(withId(R.id.credential_layout),
                                        childAtPosition(
                                                withId(R.id.topLL),
                                                0)),
                                1)));
        appCompatEditText3.perform(scrollTo(), replaceText("niharika2@gmail.com"));

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.editTextEmail), withText("niharika2@gmail.com"),
                        childAtPosition(
                                allOf(withId(R.id.credential_layout),
                                        childAtPosition(
                                                withId(R.id.topLL),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatEditText4.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.editTextPassword),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.credential_layout),
                                        2),
                                0)));
        appCompatEditText5.perform(scrollTo(), replaceText("password"), closeSoftKeyboard());

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(R.id.button_submit), withText("Login"),
                        childAtPosition(
                                allOf(withId(R.id.credential_layout),
                                        childAtPosition(
                                                withId(R.id.topLL),
                                                0)),
                                3)));
        appCompatButton6.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction navigationMenuItemView = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.design_navigation_view),
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0)),
                        8),
                        isDisplayed()));
        navigationMenuItemView.perform(click());
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
}
