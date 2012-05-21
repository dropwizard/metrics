package com.yammer.metrics.sigar.tests;

import java.io.File;
import java.util.List;

import com.yammer.metrics.sigar.FilesystemMetrics;
import com.yammer.metrics.sigar.FilesystemMetrics.FileSystem;
import com.yammer.metrics.sigar.SigarMetrics;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FilesystemMetricsTest extends CheckSigarLoadsOk {
    private final double MARGIN_BYTES = 1024 * 1024 * 50; // 50MB

    private FilesystemMetrics fsm;

    @Before
    public void setUp() {
        fsm = SigarMetrics.getInstance().filesystems();
    }

    @Test
    public void usageNumbersApproximatelyMatchThoseReturnedByJavaFile() throws Exception {
        File[] roots = File.listRoots();
        List<FileSystem> fss = fsm.filesystems();
        for (File root: roots) {
            for (FileSystem fs: fss) {
                if (new File(fs.mountPoint()).equals(root)) {
                    System.out.println("Testing filesystem mounted at " + fs.mountPoint());
                    assertThat((double) (root.getTotalSpace()), //
                            is(closeTo((double) (fs.totalSizeKB() * 1024), MARGIN_BYTES)));
                    assertThat((double) (root.getFreeSpace()), //
                            is(closeTo((double) (fs.freeSpaceKB() * 1024), MARGIN_BYTES)));
                }
            }
        }
    }
    
}
