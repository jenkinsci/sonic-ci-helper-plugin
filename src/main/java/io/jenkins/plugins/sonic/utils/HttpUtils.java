package io.jenkins.plugins.sonic.utils;

import com.ejlchina.data.TypeRef;
import com.ejlchina.okhttps.*;
import com.ejlchina.okhttps.Process;
import com.ejlchina.okhttps.gson.GsonMsgConvertor;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.Secret;
import io.jenkins.plugins.sonic.Messages;
import io.jenkins.plugins.sonic.SonicGlobalConfiguration;
import io.jenkins.plugins.sonic.bean.*;
import io.jenkins.plugins.sonic.bean.HttpResult;
import org.apache.tools.ant.DirectoryScanner;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HttpUtils {
    private static final String UPLOAD_URL = "/api/folder/upload";
    private static final String PROJECT_URL = "/api/controller/projects/list";
    private static final String PACKAGE_URL = "/api/controller/packages";
    private static final String SonicToken = "SonicToken";
    private static final String BRANCH = "${GIT_BRANCH}";
    private static final HTTP http = HTTP.builder()
            .bodyType("json")
            .addMsgConvertor(new GsonMsgConvertor())
            .build();


    public static boolean upload(AbstractBuild<?, ?> build, BuildListener listener, ParamBean paramBean) throws IOException, InterruptedException {

        paramBean.setHost(build.getEnvironment(listener).expand(paramBean.getHost()));
        paramBean.setApiKey(build.getEnvironment(listener).expand(paramBean.getApiKey()));
        paramBean.setWildcard(build.getEnvironment(listener).expand(paramBean.getWildcard()));
        paramBean.setScanDir(build.getEnvironment(listener).expand(paramBean.getScanDir()));
        paramBean.setUpdateDescription(build.getEnvironment(listener).expand(paramBean.getUpdateDescription()));
        paramBean.setQrcodePath(build.getEnvironment(listener).expand(paramBean.getQrcodePath()));

        if (!StringUtils.hasText(paramBean.getProjectId())) {
            Logging.logging(listener, Messages.UploadBuilder_Http_error_missProjectId());
            return false;
        }

        String path = findFile(paramBean.getScanDir(), paramBean.getWildcard(), listener);
        if (!StringUtils.hasText(path)) {
            Logging.logging(listener, Messages.UploadBuilder_Http_error_missFile());
            return false;
        }
        File uploadFile = new File(path);
        if (!uploadFile.exists() || !uploadFile.isFile()) {
            Logging.logging(listener, Messages.UploadBuilder_Http_error_missFile());
            return false;
        }
        Logging.printHeader(listener);
        String url = upload(build, uploadFile, listener, paramBean);

        if (url == null) {
            return false;
        }
        String branch = getBranch(build, listener);
        savePackageInfo(paramBean, listener, uploadFile.getName(), url, platform(uploadFile.getName()), branch);
        return true;
    }

    private static String getBranch(AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
        String branch = build.getEnvironment(listener).expand(BRANCH);

        if (BRANCH.equals(branch)) {
            return "unknown";
        }
        return branch;

    }

    private static String upload(AbstractBuild<?, ?> build, File uploadFile, BuildListener listener ,ParamBean paramBean) {
        HttpCall call =buildHttp(paramBean, UPLOAD_URL)
                .addFilePara("file", uploadFile)
                .addBodyPara("type", "packageFiles")
                .stepRate(0.05)    // 设置每发送 1% 执行一次进度回调（不设置以 StepBytes 为准）
                .setOnProcess(new OnCallback<Process>() {
                    @Override
                    public void on(Process process) {
                        Logging.logging(listener, "upload progress: " + (int)(process.getRate() * 100) + " %");
                    }
                })
                .setOnException(new OnCallback<IOException>() {
                    @Override
                    public void on(IOException e) {
                        Logging.logging(listener, "upload exception: " );
                        Logging.logging(listener, e.fillInStackTrace().toString());
                    }
                })
                .post();
        if (call.getResult().isSuccessful()) {
            HttpResult<String> httpResult = call.getResult().getBody().toBean(new TypeRef<HttpResult<String>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
            Logging.logging(listener, "${appURL}: " + httpResult.getData());
            build.addAction(new PublishEnvVarAction("appURL", httpResult.getData()));

            return httpResult.getData();
        }
        Logging.logging(listener, "==============");
        Logging.logging(listener, "upload file fail");
        Logging.logging(listener, call.getResult().toString());
        Logging.logging(listener, "==============");
        return null;
    }

    private static void savePackageInfo(ParamBean paramBean, BuildListener listener, String name,
                                        String url, String platform, String branch) {

        PackageBean packageBean = new PackageBean();
        packageBean.setPkgName(name);
        packageBean.setUrl(url);
        packageBean.setPlatform(platform);
        packageBean.setProjectId(Integer.parseInt(paramBean.getProjectId()));
        packageBean.setBranch(branch);

        com.ejlchina.okhttps.HttpResult result = http.sync(paramBean.getHost() + PACKAGE_URL)
                .addHeader(SonicToken, paramBean.getApiKey())
                .setBodyPara(packageBean)
                .put();


        if (result.isSuccessful()) {
            HttpResult<String> httpResult = result.getBody().toBean(new TypeRef<HttpResult<String>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
            Logging.logging(listener, "==============");
            Logging.logging(listener, "安装包保存成功");
            Logging.logging(listener, httpResult.toString());
        }else {
            Logging.logging(listener, "==============");
            Logging.logging(listener, "安装包保存失败");
            Logging.logging(listener, result.toString());

        }
        Logging.logging(listener, "==============");

    }

    private static AHttpTask buildHttp(ParamBean paramBean, String uri) {
        return http.async(paramBean.getHost() + uri)
                .addHeader(SonicToken, paramBean.getApiKey());
    }

    public static HttpResult<List<Project>> listProject(String apiKey) {
        String currentApiKey = SonicGlobalConfiguration.planApiKey(Secret.fromString(apiKey));
        String host = SonicGlobalConfiguration.get().getHost();

        if (currentApiKey == null || host == null) {
            throw new AssertionError("api key or host is null");
        }
        return http.sync(host + PROJECT_URL)
                .addHeader(SonicToken, currentApiKey)
                .get()
                .getBody()
                .toBean(new TypeRef<HttpResult<List<Project>>>() {
                    @Override
                    public Type getType() {
                        return super.getType();
                    }
                });

    }


    public static String findFile(String scandir, String wildcard,BuildListener listener ) {
        File dir = new File(scandir);
        if (!dir.exists() || !dir.isDirectory()) {
            Logging.logging(listener, "scan dir:" + dir.getAbsolutePath());
            Logging.logging(listener, "scan dir isn't exist or it's not a directory!");
            return null;
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(scandir);
        scanner.setIncludes(new String[]{wildcard});
        scanner.setCaseSensitive(true);
        scanner.scan();
        String[] uploadFiles = scanner.getIncludedFiles();

        if (uploadFiles == null || uploadFiles.length == 0) {
            return null;
        }
        if (uploadFiles.length == 1) {
            return new File(dir, uploadFiles[0]).getAbsolutePath();
        }

        List<String> strings = Arrays.asList(uploadFiles);
        Collections.sort(strings, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                File file1 = new File(dir, o1);
                File file2 = new File(dir, o2);
                return Long.compare(file2.lastModified(), file1.lastModified());
            }
        });
        String uploadFiltPath = new File(dir, strings.get(0)).getAbsolutePath();
        Logging.logging(listener, "Found " + uploadFiles.length + " files, the default choice of the latest modified file!");
        Logging.logging(listener, "The latest modified file is " + uploadFiltPath );
        return uploadFiltPath;
    }


    private static String platform(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf("."));

        if (!StringUtils.hasText(ext)) {
            return "unknown";
        }
        if (ext.contains("ipa")) {
            return "iOS";
        }
        return "Android";
    }
}
