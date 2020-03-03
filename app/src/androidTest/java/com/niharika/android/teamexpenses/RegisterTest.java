package com.niharika.android.teamexpenses;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.niharika.android.groupexpensetracker.MainActivity;
import com.niharika.android.groupexpensetracker.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class RegisterTest {
    private static final String EMAIL ="niharikatest5@gmail.com" ,PASS="password";
    @Rule
    public ActivityTestRule mActivityTestRule= new ActivityTestRule<>(MainActivity.class);


@Test
public void register() throws InterruptedException {
    onView(withId(R.id.editTextEmail))
            .perform(typeText(EMAIL), closeSoftKeyboard());
    onView(withId(R.id.editTextPassword))
            .perform(typeText(PASS), closeSoftKeyboard());
    onView(withId(R.id.button_submit)).perform(click());
    onView(withId(R.id.loading_msg)).check(matches(isDisplayed()));
}
}