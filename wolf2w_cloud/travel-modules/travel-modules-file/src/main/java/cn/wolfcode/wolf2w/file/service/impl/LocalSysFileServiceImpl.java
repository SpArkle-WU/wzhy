package cn.wolfcode.wolf2w.file.service.impl;

import cn.wolfcode.wolf2w.file.service.ISysFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import cn.wolfcode.wolf2w.file.utils.FileUploadUtils;

/**
 * 本地文件存储
 * 
 * @author ruoyi
 */
@Service
@ConditionalOnProperty(prefix = "store", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalSysFileServiceImpl implements ISysFileService
{
    /**
     * 资源映射路径 前缀（默认值兜底，避免 @Value 占位符缺失启动失败）
     */
    @Value("${file.prefix:/static/}")
    public String localFilePrefix;

    /**
     * 域名或本机访问地址（默认值兜底）
     */
    @Value("${file.domain:http://127.0.0.1:8086}")
    public String domain;

    /**
     * 上传文件存储在本地的根路径（默认值兜底）
     */
    @Value("${file.path:D:/wolf2w/uploadPath/}")
    private String localFilePath;

    /**
     * 本地文件上传接口
     * 
     * @param file 上传的文件
     * @return 访问地址
     * @throws Exception
     */
    @Override
    public String uploadFile(MultipartFile file) throws Exception
    {
        String name = FileUploadUtils.upload(localFilePath, file);
        String url = domain + localFilePrefix + name;
        return url;
    }
}
