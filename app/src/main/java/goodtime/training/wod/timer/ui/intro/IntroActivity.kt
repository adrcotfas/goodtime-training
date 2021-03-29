package goodtime.training.wod.timer.ui.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.github.appintro.model.SliderPage
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.ResourcesHelper

class IntroActivity : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setImmersiveMode()

        setNextArrowColor(ResourcesHelper.grey500)
        setSkipArrowColor(ResourcesHelper.grey500)

        // Change Indicator Color
        setIndicatorColor(
            selectedIndicatorColor = ResourcesHelper.grey500,
            unselectedIndicatorColor = ResourcesHelper.grey200
        )

        addSlide(
            AppIntroFragment.newInstance(
                "Welcome",
                "Your training assistant for home,\ngym or outside use is here to help.",
                titleColor = ResourcesHelper.grey1000,
                descriptionColor = ResourcesHelper.grey800,
                backgroundColor = ResourcesHelper.grey50,
                imageDrawable = R.drawable.illustration_jump_rope,
                titleTypefaceFontRes = R.font.app_font,
                descriptionTypefaceFontRes = R.font.app_font
            )
        )

        addSlide(
            AppIntroFragment.newInstance(
                SliderPage(
                    "Improve your fitness",
                    "Use workout reminders to build a habit,\nset a weekly goal and track your active time.",
                    titleColor = ResourcesHelper.grey1200,
                    descriptionColor = ResourcesHelper.grey1000,
                    backgroundColor = ResourcesHelper.grey50,
                    imageDrawable = R.drawable.illustration_phone,
                    titleTypefaceFontRes = R.font.app_font,
                    descriptionTypefaceFontRes = R.font.app_font
                )
            )
        )

        addSlide(
            AppIntroFragment.newInstance(
                "Configurable timer",
                "Enjoy the good type of pain and sweat\nwith different timer types.",
                titleColor = ResourcesHelper.grey1200,
                descriptionColor = ResourcesHelper.grey1000,
                backgroundColor = ResourcesHelper.grey50,
                imageDrawable = R.drawable.illustration_front_squat,
                titleTypefaceFontRes = R.font.app_font,
                descriptionTypefaceFontRes = R.font.app_font
            )
        )

        setTransformer(
            AppIntroPageTransformerType.Parallax(
            titleParallaxFactor = 10.0,
            imageParallaxFactor = -5.0,
            descriptionParallaxFactor = 10.0))
    }

    public override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }

    public override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }
}