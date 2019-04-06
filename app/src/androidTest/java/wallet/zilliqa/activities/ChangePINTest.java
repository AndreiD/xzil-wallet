package wallet.zilliqa.activities;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import wallet.zilliqa.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ChangePINTest {

  @Rule
  public ActivityTestRule<SplashActivity> mActivityTestRule = new ActivityTestRule<>(SplashActivity.class);

  @Rule
  public GrantPermissionRule mGrantPermissionRule =
      GrantPermissionRule.grant(
          "android.permission.CAMERA",
          "android.permission.READ_EXTERNAL_STORAGE");

  @Test
  public void changePINTest() {
    ViewInteraction appCompatButton = onView(
        allOf(withId(android.R.id.button1), withText("OK"),
            childAtPosition(
                childAtPosition(
                    withId(R.id.buttonPanel),
                    0),
                3)));
    appCompatButton.perform(scrollTo(), click());

    ViewInteraction appCompatButton2 = onView(
        allOf(withId(android.R.id.button1), withText("I Agree"),
            childAtPosition(
                childAtPosition(
                    withId(R.id.buttonPanel),
                    0),
                3)));
    appCompatButton2.perform(scrollTo(), click());

    ViewInteraction appCompatButton3 = onView(
        allOf(withId(R.id.button_new_account_import), withText("IMPORT WALLET"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    1),
                2),
            isDisplayed()));
    appCompatButton3.perform(click());

    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

    ViewInteraction appCompatTextView = onView(
        allOf(withId(R.id.title), withText("Settings"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                    0),
                0),
            isDisplayed()));
    appCompatTextView.perform(click());

    DataInteraction linearLayout = onData(anything())
        .inAdapterView(allOf(withId(android.R.id.list),
            childAtPosition(
                withId(android.R.id.list_container),
                0)))
        .atPosition(2);
    linearLayout.perform(click());

    ViewInteraction appCompatEditText = onView(
        allOf(withId(R.id.edit_settings_old_pin),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    2),
                0),
            isDisplayed()));
    appCompatEditText.perform(click());

    ViewInteraction appCompatEditText2 = onView(
        allOf(withId(R.id.edit_settings_old_pin),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    2),
                0),
            isDisplayed()));
    appCompatEditText2.perform(replaceText("1"), closeSoftKeyboard());

    ViewInteraction appCompatEditText3 = onView(
        allOf(withId(R.id.edit_settings_old_pin), withText("1"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    2),
                0),
            isDisplayed()));
    appCompatEditText3.perform(click());

    ViewInteraction appCompatEditText4 = onView(
        allOf(withId(R.id.edit_settings_old_pin), withText("1"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    2),
                0),
            isDisplayed()));
    appCompatEditText4.perform(replaceText("123"));

    ViewInteraction appCompatEditText5 = onView(
        allOf(withId(R.id.edit_settings_old_pin), withText("123"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    2),
                0),
            isDisplayed()));
    appCompatEditText5.perform(closeSoftKeyboard());

    ViewInteraction appCompatEditText6 = onView(
        allOf(withId(R.id.edit_settings_old_pin), withText("123"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    2),
                0),
            isDisplayed()));
    appCompatEditText6.perform(click());

    ViewInteraction appCompatEditText7 = onView(
        allOf(withId(R.id.edit_settings_old_pin), withText("123"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    2),
                0),
            isDisplayed()));
    appCompatEditText7.perform(replaceText("123123"));

    ViewInteraction appCompatEditText8 = onView(
        allOf(withId(R.id.edit_settings_old_pin), withText("123123"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    2),
                0),
            isDisplayed()));
    appCompatEditText8.perform(closeSoftKeyboard());

    ViewInteraction appCompatEditText9 = onView(
        allOf(withId(R.id.edit_settings_new_pin),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    2),
                1),
            isDisplayed()));
    appCompatEditText9.perform(replaceText("111111"), closeSoftKeyboard());

    ViewInteraction appCompatEditText10 = onView(
        allOf(withId(R.id.edit_settings_new_pin2),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    2),
                2),
            isDisplayed()));
    appCompatEditText10.perform(replaceText("111111"), closeSoftKeyboard());

    ViewInteraction appCompatButton4 = onView(
        allOf(withId(R.id.btn_update_pin_change), withText("Change PIN"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    3),
                0),
            isDisplayed()));
    appCompatButton4.perform(click());

    pressBack();

    pressBack();

    pressBack();

    pressBack();
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
