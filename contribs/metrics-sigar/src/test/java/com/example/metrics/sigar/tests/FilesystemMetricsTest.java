package com.example.metrics.sigar.tests;

import java.io.File;
import java.util.List;

import com.example.metrics.sigar.FilesystemMetrics;
import com.example.metrics.sigar.FilesystemMetrics.FileSystem;
import com.example.metrics.sigar.SigarMetrics;

import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FilesystemMetricsTest {
    private final FilesystemMetrics fsm = SigarMetrics.getInstance().filesystems();

    private final double MARGIN_BYTES = 1024 * 1024 * 50; // 50MB

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
