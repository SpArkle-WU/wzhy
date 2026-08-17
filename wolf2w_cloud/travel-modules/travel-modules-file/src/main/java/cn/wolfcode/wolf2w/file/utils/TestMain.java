package cn.wolfcode.wolf2w.file.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

/**
 * 阿里云OSS上传测试类
 * 密钥从 application-local.yml 同级目录的 oss-test.properties 读取，避免硬编码到源码中
 * 运行前请在项目根目录创建 oss-test.properties 文件（该文件已在 .gitignore 中忽略）
 */
public class TestMain {

    public static void main(String[] args) {

        // 从外部配置文件读取密钥，避免源码中硬编码敏感信息
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("oss-test.properties")) {
            props.load(in);
        } catch (Exception e) {
            System.out.println("未找到 oss-test.properties，请在项目根目录创建该文件并填写阿里云OSS配置");
            e.printStackTrace();
            return;
        }

        // 从配置文件读取参数
        String endpoint = props.getProperty("oss.endpoint");
        String bucketName = props.getProperty("oss.bucket-name");
        String accessKeyId = props.getProperty("oss.access-key-id");
        String accessKeySecret = props.getProperty("oss.access-key-secret");

        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = "640.png";
        // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。
        String filePath = "E:\\Share\\640.png";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new File(filePath));

            // 上传文件。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            System.out.println("上传成功");
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
