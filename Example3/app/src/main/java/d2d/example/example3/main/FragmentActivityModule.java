package d2d.example.example3.main;

import androidx.fragment.app.FragmentActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class FragmentActivityModule {
    private final FragmentActivity fragmentActivity;

    public FragmentActivityModule(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
    }

    @Provides
    FragmentActivity provideFragmentActivity() {
        return fragmentActivity;
    }
}
