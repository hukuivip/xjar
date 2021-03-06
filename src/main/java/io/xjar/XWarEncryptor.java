package io.xjar;

import io.xjar.key.XKey;
import io.xjar.war.XWarAllFilter;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.Deflater;

/**
 * Java Web WAR包加密器
 *
 * @author Payne 646742615@qq.com
 * 2018/11/22 15:27
 */
public class XWarEncryptor extends XEntryEncryptor<JarArchiveEntry> implements XEncryptor, XConstants {
    // 安全过滤器，避免由于用户自定义过滤器时把其他无关资源加密了造成无法运行
    private final XJarArchiveEntryFilter safeFilter = new XWarAllFilter();
    private final int level;

    public XWarEncryptor(XEncryptor xEncryptor, XJarArchiveEntryFilter... filters) {
        this(xEncryptor, Arrays.asList(filters));
    }

    public XWarEncryptor(XEncryptor xEncryptor, Collection<XJarArchiveEntryFilter> filters) {
        this(xEncryptor, Deflater.DEFLATED, filters);
    }

    public XWarEncryptor(XEncryptor xEncryptor, int level, XJarArchiveEntryFilter... filters) {
        this(xEncryptor, level, Arrays.asList(filters));
    }

    public XWarEncryptor(XEncryptor xEncryptor, int level, Collection<XJarArchiveEntryFilter> filters) {
        super(xEncryptor, filters);
        this.level = level;
    }

    @Override
    public void encrypt(XKey key, File src, File dest) throws IOException {
        try (
                FileInputStream fis = new FileInputStream(src);
                FileOutputStream fos = new FileOutputStream(dest)
        ) {
            encrypt(key, fis, fos);
        }
    }

    @Override
    public void encrypt(XKey key, InputStream in, OutputStream out) throws IOException {
        JarArchiveInputStream zis = null;
        JarArchiveOutputStream zos = null;
        Set<String> indexes = new LinkedHashSet<>();
        try {
            zis = new JarArchiveInputStream(in);
            zos = new JarArchiveOutputStream(out);
            zos.setLevel(level);
            NoCloseInputStream nis = new NoCloseInputStream(zis);
            NoCloseOutputStream nos = new NoCloseOutputStream(zos);
            XJarEncryptor xJarEncryptor = new XJarEncryptor(xEncryptor, level);
            JarArchiveEntry entry;
            while ((entry = zis.getNextJarEntry()) != null) {
                if (entry.getName().startsWith(XJAR_SRC_DIR)
                        || entry.getName().endsWith(XJAR_INF_DIR)
                        || entry.getName().endsWith(XJAR_INF_DIR + XJAR_INF_IDX)
                ) {
                    continue;
                }
                if (entry.isDirectory()) {
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                } else if (entry.getName().endsWith(".jar")) {
                    JarArchiveEntry jar = new JarArchiveEntry(entry.getName());
                    jar.setTime(entry.getTime());
                    zos.putArchiveEntry(jar);
                    boolean filtered = filter(entry);
                    XEncryptor encryptor = filtered ? xJarEncryptor : xNopEncryptor;
                    encryptor.encrypt(key, nis, nos);
                } else {
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                    boolean filtered = filter(entry);
                    if (filtered) indexes.add(entry.getName());
                    XEncryptor encryptor = filtered ? this : xNopEncryptor;
                    try (InputStream eis = encryptor.encrypt(key, nis)) {
                        XKit.transfer(eis, nos);
                    }
                }
                zos.closeArchiveEntry();
            }

            if (!indexes.isEmpty()) {
                String classpath = "WEB-INF/classes/";
                JarArchiveEntry XJAR_INF = new JarArchiveEntry(classpath + XJAR_INF_DIR);
                XJAR_INF.setTime(System.currentTimeMillis());
                zos.putArchiveEntry(XJAR_INF);
                zos.closeArchiveEntry();

                JarArchiveEntry IDX = new JarArchiveEntry(classpath + XJAR_INF_DIR + XJAR_INF_IDX);
                IDX.setTime(System.currentTimeMillis());
                zos.putArchiveEntry(IDX);
                int idx = classpath.length();
                for (String index : indexes) {
                    nos.write(index.substring(idx).getBytes());
                    nos.write(CRLF.getBytes());
                }
                zos.closeArchiveEntry();
            }

            zos.finish();
        } finally {
            XKit.close(zis);
            XKit.close(zos);
        }
    }

    @Override
    public boolean filter(JarArchiveEntry entry) {
        return super.filter(entry) && safeFilter.filter(entry);
    }
}
