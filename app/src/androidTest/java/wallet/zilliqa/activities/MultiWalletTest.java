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
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import wallet.zilliqa.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MultiWalletTest {

  @Rule
  public ActivityTestRule<SplashActivity> mActivityTestRule = new ActivityTestRule<>(SplashActivity.class);

  @Rule
  public GrantPermissionRule mGrantPermissionRule =
      GrantPermissionRule.grant(
          "android.permission.CAMERA",
          "android.permission.READ_EXTERNAL_STORAGE");

  @Test
  public void multiWalletTest() {
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

    ViewInteraction actionMenuItemView = onView(
        allOf(withId(R.id.action_manage_wallets), withContentDescription("Manage Wallets"),
            childAtPosition(
                childAtPosition(
                    withId(R.id.theToolbar),
                    1),
                1),
            isDisplayed()));
    actionMenuItemView.perform(click());

    ViewInteraction appCompatImageButton = onView(
        allOf(childAtPosition(
            allOf(withId(R.id.toolbar),
                childAtPosition(
                    withClassName(is("android.widget.RelativeLayout")),
                    0)),
            0),
            isDisplayed()));
    appCompatImageButton.perform(click());

    ViewInteraction actionMenuItemView2 = onView(
        allOf(withId(R.id.action_add), withContentDescription("Settings"),
            childAtPosition(
                childAtPosition(
                    withId(R.id.theToolbar),
                    1),
                0),
            isDisplayed()));
    actionMenuItemView2.perform(click());

    ViewInteraction appCompatButton4 = onView(
        allOf(withId(R.id.button_new_account), withText("GENERATE NEW WALLET"),
            childAtPosition(
                allOf(withId(R.id.linlayout_fragmenthome_public_balance),
                    childAtPosition(
                        withClassName(is("android.widget.LinearLayout")),
                        0)),
                1),
            isDisplayed()));
    appCompatButton4.perform(click());

    ViewInteraction appCompatCheckBox = onView(
        allOf(withId(R.id.checkBox_seed), withText("I confirm that I stored it."),
            childAtPosition(
                allOf(withId(R.id.linLayout_new_account_all),
                    childAtPosition(
                        withClassName(is("android.widget.RelativeLayout")),
                        0)),
                3),
            isDisplayed()));
    appCompatCheckBox.perform(click());

    ViewInteraction appCompatButton8 = onView(
        allOf(withId(R.id.btn_continue), withText("Continue"),
            childAtPosition(
                childAtPosition(
                    withId(R.id.linLayout_new_account_all),
                    4),
                1),
            isDisplayed()));
    appCompatButton8.perform(click());

    ViewInteraction actionMenuItemView3 = onView(
        allOf(withId(R.id.action_manage_wallets), withContentDescription("Manage Wallets"),
            childAtPosition(
                childAtPosition(
                    withId(R.id.theToolbar),
                    1),
                1),
            isDisplayed()));
    actionMenuItemView3.perform(click());

    ViewInteraction cardView = onView(
        allOf(withId(R.id.cardView_wallet),
            childAtPosition(
                childAtPosition(
                    withId(R.id.recycler_view_wallets),
                    1),
                0),
            isDisplayed()));
    cardView.perform(click());

    ViewInteraction cardView2 = onView(
        allOf(withId(R.id.cardView_wallet),
            childAtPosition(
                childAtPosition(
                    withId(R.id.recycler_view_wallets),
                    0),
                0),
            isDisplayed()));
    cardView2.perform(click());

    ViewInteraction appCompatButton9 = onView(
        allOf(withId(android.R.id.button2), withText("SET AS DEFAULT"),
            childAtPosition(
                childAtPosition(
                    withId(R.id.buttonPanel),
                    0),
                2)));
    appCompatButton9.perform(scrollTo(), click());

    ViewInteraction actionMenuItemView4 = onView(
        allOf(withId(R.id.action_manage_wallets), withContentDescription("Manage Wallets"),
            childAtPosition(
                childAtPosition(
                    withId(R.id.theToolbar),
                    1),
                1),
            isDisplayed()));
    actionMenuItemView4.perform(click());

    ViewInteraction cardView3 = onView(
        allOf(withId(R.id.cardView_wallet),
            childAtPosition(
                childAtPosition(
                    withId(R.id.recycler_view_wallets),
                    1),
                0),
            isDisplayed()));
    cardView3.perform(click());

    ViewInteraction appCompatButton10 = onView(
        allOf(withId(android.R.id.button2), withText("SET AS DEFAULT"),
            childAtPosition(
                childAtPosition(
                    withId(R.id.buttonPanel),
                    0),
                2)));
    appCompatButton10.perform(scrollTo(), click());

    ViewInteraction textView = onView(
        allOf(withId(R.id.textView_fragmentHome_status), withText("all looks good."),
            childAtPosition(
                allOf(withId(R.id.linlayoutTopBalance),
                    childAtPosition(
                        IsInstanceOf.<View>instanceOf(android.widget.RelativeLayout.class),
                        0)),
                3),
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
