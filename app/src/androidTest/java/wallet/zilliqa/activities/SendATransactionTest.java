package wallet.zilliqa.activities;

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

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SendATransactionTest {

  @Rule
  public ActivityTestRule<SplashActivity> mActivityTestRule = new ActivityTestRule<>(SplashActivity.class);

  @Rule
  public GrantPermissionRule mGrantPermissionRule =
      GrantPermissionRule.grant(
          "android.permission.CAMERA",
          "android.permission.READ_EXTERNAL_STORAGE");

  @Test
  public void sendATransactionTest() {
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

    ViewInteraction bottomBarTab = onView(
        allOf(withText("Send"),
            childAtPosition(
                allOf(withId(R.id.bb_bottom_bar_item_container),
                    childAtPosition(
                        withId(R.id.bb_bottom_bar_outer_container),
                        1)),
                0),
            isDisplayed()));
    bottomBarTab.perform(click());

    ViewInteraction appCompatEditText = onView(
        allOf(withId(R.id.send_editText_amount), withText("50"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    4),
                0)));
    appCompatEditText.perform(scrollTo(), replaceText("0.1"));

    ViewInteraction appCompatEditText2 = onView(
        allOf(withId(R.id.send_editText_amount), withText("0.1"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    4),
                0),
            isDisplayed()));
    appCompatEditText2.perform(closeSoftKeyboard());

    pressBack();

    ViewInteraction appCompatButton4 = onView(
        allOf(withId(R.id.send_button_send), withText("CONTINUE"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    8),
                0)));
    appCompatButton4.perform(scrollTo(), click());

    pressBack();

    ViewInteraction appCompatButton5 = onView(
        allOf(withId(R.id.btn_dlg_confirm_send), withText("SEND"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    7),
                0),
            isDisplayed()));
    appCompatButton5.perform(click());

    ViewInteraction textView = onView(
        allOf(withId(R.id.textView_dlg_status), withText("Status: Pending"),
            childAtPosition(
                childAtPosition(
                    withId(android.R.id.content),
                    0),
                1),
            isDisplayed()));
    textView.check(matches(isDisplayed()));
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
