package hudson.plugins.git;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Items;
import hudson.model.TransientProjectActionFactory;
import hudson.plugins.git.util.Build;
import hudson.plugins.git.util.BuildData;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Maintain the branch / revision Map for git-based projects.
 * <p>
 * Compared to (legacy) {@link BuildData}, there is a single BuildHistory per project, centralizing build history
 * and using a dedicated file for persistence.
 *
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@ExportedBean(defaultVisibility = 999)
public class BuildHistory extends BuildData {

    private final AbstractProject project;

    private BuildHistory(AbstractProject p) {
        this.project = p;
        File file = new File(project.getRootDir(), BuildData.class.getName() + ".xml");
        if (file.exists()) {
            try {
                buildsByBranchName = (Map<String, Build>) Items.XSTREAM2.fromXML(new FileReader(file));
            } catch (FileNotFoundException e) {
                // really ?
                throw new RuntimeException("failed to load "+file+" exists but FileNotFoundException", e);
            }
        }
    }


    /**
     * Register BuildData action on git-enabled projects to maintain the branch::revision Map
     */
    @Extension
    public static class ActionFactory extends TransientProjectActionFactory {

        @Override
        public Collection<? extends Action> createFor(AbstractProject p) {
            if (p.getScm() instanceof GitSCM) {
                return Collections.singleton(new BuildHistory(p));
            }
            return Collections.EMPTY_LIST;
        }
    }
}
