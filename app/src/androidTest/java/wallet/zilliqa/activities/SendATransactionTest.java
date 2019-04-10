package wallet.zilliqa.activities;

import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import wallet.zilliqa.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.schibsted.spain.barista.interaction.BaristaSleepInteractions.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.allOf;

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

    ViewInteraction bottomBarTab = onView(
        allOf(withText("Send"),
            isDisplayed()));
    bottomBarTab.perform(click());

    sleep(2, SECONDS);

    ViewInteraction appCompatButton4 = onView(
        allOf(withId(R.id.send_button_send), withText("CONTINUE")));
    appCompatButton4.perform(scrollTo(), click());

    ViewInteraction appCompatButtonx5 = onView(
        allOf(withText("VERIFY")));
    appCompatButtonx5.perform(scrollTo(), click());

    pressBack();

    ViewInteraction appCompatButton5 = onView(
        allOf(withId(R.id.btn_dlg_confirm_send), withText("SEND")));
    appCompatButton5.perform(click());

    ViewInteraction textView = onView(
        allOf(withId(R.id.textView_dlg_status), withText("Status: Pending"),
            isDisplayed()));
    textView.check(matches(isDisplayed()));
  }
}
