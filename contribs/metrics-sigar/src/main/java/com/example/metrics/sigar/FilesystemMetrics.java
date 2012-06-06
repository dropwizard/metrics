package com.example.metrics.sigar;

import java.util.List;
import java.util.ArrayList;

import com.yammer.metrics.core.MetricsRegistry;

import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class FilesystemMetrics extends AbstractSigarMetric {

    public enum FSType {
        // Ordered so that ordinals match the constants
        // in org.hyperic.sigar.FileSystem
        Unknown, None, LocalDisk, Network, Ramdisk, Cdrom, Swap
    }

    public static final class FileSystem {
        private final String deviceName;
        private final String mountPoint;
        private final FSType genericFSType;
        private final String osSpecificFSType;
        private final long totalSizeKB;
        private final long freeSpaceKB;
    
        public FileSystem( //
                String deviceName, String mountPoint, //
                FSType genericFSType, String osSpecificFSType, //
                long totalSizeKB, long freeSpaceKB) {
            this.deviceName = deviceName;
            this.mountPoint = mountPoint;
            this.genericFSType = genericFSType;
            this.osSpecificFSType = osSpecificFSType;
            this.totalSizeKB = totalSizeKB;
            this.freeSpaceKB = freeSpaceKB;
        }

        public static FileSystem fromSigarBean(org.hyperic.sigar.FileSystem fs,
                long totalSizeKB, long freeSpaceKB) {
            return new FileSystem( //
                    fs.getDevName(), fs.getDirName(), //
                    FSType.values()[fs.getType()], fs.getSysTypeName(), //
                    totalSizeKB, freeSpaceKB);
        }

        public String deviceName() { return deviceName; }
        public String mountPoint() { return mountPoint; }
        public FSType genericFSType() { return genericFSType; }
        public String osSpecificFSType() { return osSpecificFSType; }
        public long totalSizeKB() { return totalSizeKB; }
        public long freeSpaceKB() { return freeSpaceKB; }
    }

    public void registerGauges(MetricsRegistry registry) {
        // Do not register any gauges
    }

    protected FilesystemMetrics(Sigar sigar) {
        super(sigar);
    }

    public List<FileSystem> filesystems() {
        List<FileSystem> result = new ArrayList<FileSystem>();
        org.hyperic.sigar.FileSystem[] fss = null;
        try {
            fss = sigar.getFileSystemList(); 
        } catch (SigarException e) {
            // give up
            return result;
        }

        if (fss == null) {
            return result;
        }

        for (org.hyperic.sigar.FileSystem fs: fss) {
            long totalSizeKB = 0L;
            long freeSpaceKB = 0L;
            try {
                FileSystemUsage usage = sigar.getFileSystemUsage(fs.getDirName());
                totalSizeKB = usage.getTotal();
                freeSpaceKB = usage.getFree();
            } catch (SigarException e) {
                // ignore
            }
            result.add(FileSystem.fromSigarBean(fs, totalSizeKB, freeSpaceKB));
        } 
        return result;
   }

}
