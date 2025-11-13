package cn.cxdproject.coder.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * 资源压缩工具类
 *
 * @author heathcetide
 */
@Slf4j
@Service
public class ResourceCompressor {
    
    public void compressFiles(List<File> files, String outputPath) {
        try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(new File(outputPath))) {
            for (File file : files) {
                addToZip(file, file.getName(), zipOut);
            }
        } catch (IOException e) {
            log.error("压缩文件失败", e);
        }
    }
    
    private void addToZip(File file, String entryName, ZipArchiveOutputStream zipOut) 
            throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(file, entryName);
        zipOut.putArchiveEntry(entry);
        
        if (file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    zipOut.write(buffer, 0, bytesRead);
                }
            }
        }
        
        zipOut.closeArchiveEntry();
    }
} 