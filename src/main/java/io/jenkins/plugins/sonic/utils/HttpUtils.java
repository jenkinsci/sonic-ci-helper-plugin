/*
 *  Copyright (C) [SonicCloudOrg] Sonic Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package io.jenkins.plugins.sonic.utils;

import com.ejlchina.data.TypeRef;
import com.ejlchina.okhttps.*;
import com.ejlchina.okhttps.gson.GsonMsgConvertor;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.Secret;
import io.jenkins.plugins.sonic.Messages;
import io.jenkins.plugins.sonic.SonicGlobalConfiguration;
import io.jenkins.plugins.sonic.bean.*;
import io.jenkins.plugins.sonic.bean.HttpResult;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HttpUtils {
    private static final String UPLOAD_URL = "/server/api/folder/upload";
    private static final String PROJECT_URL = "/server/api/controller/projects/list";
    private static final String PACKAGE_URL = "/server/api/controller/packages";
    private static final String RUN_SUITE_URL = "/server/api/controller/testSuites/runSuite";
    private static final String SonicToken = "SonicToken";
    private static final String BRANCH = "${GIT_BRANCH}";
    private static final String BUILD_URL = "${BUILD_URL}";
    private static final HTTP http = HTTP.builder()
            .bodyType("json")
            .addMsgConvertor(new GsonMsgConvertor())
            .build();


    public static boolean uploadAction(AbstractBuild<?, ?> build, BuildListener listener, ParamBean paramBean) throws IOException, InterruptedException {
        paramBean.setHost(build.getEnvironment(listener).expand(paramBean.getHost()));
        paramBean.setApiKey(Secret.fromString(build.getEnvironment(listener).expand(Secret.toString(paramBean.getApiKey()))));
        paramBean.setScanDir(build.getEnvironment(listener).expand(paramBean.getScanDir()));

        if (!StringUtils.hasText(paramBean.getProjectId())) {
            Logging.logging(listener, Messages.UploadBuilder_Http_error_missProjectId());
            return false;
        }

        FilePath path = findFile(paramBean.getWorkspace(), paramBean.getScanDir(), listener);
        if (null == path) {
            Logging.logging(listener, Messages.UploadBuilder_Http_error_missFile());
            return false;
        } else {
            Logging.logging(listener, Messages.UploadBuilder_Scan_result() + path.getRemote());
        }

        final String fileName = path.getName();
        String url = uploadAction(build, path, listener, paramBean);

        if (url == null) {
            return false;
        }
        String branch = getBranch(build, listener);
        String buildUrl = getBuildUrl(build, listener);
        savePackageInfo(paramBean, listener, fileName, url, platform(fileName), branch, buildUrl);
        if (StringUtils.hasText(paramBean.getSuiteId())) {
            try {
                int suiteId = Integer.parseInt(paramBean.getSuiteId());
                runSuite(paramBean, listener, suiteId);
            } catch (Exception e) {
                Logging.logging(listener, e.getMessage());
                e.printStackTrace();
            }
        }
        return true;
    }

    private static String getBranch(AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
        String branch = build.getEnvironment(listener).expand(BRANCH);

        if (BRANCH.equals(branch)) {
            return "unknown";
        }
        return branch;
    }

    private static String getBuildUrl(AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
        String buildUrl = build.getEnvironment(listener).expand(BUILD_URL);

        if (BUILD_URL.equals(buildUrl)) {
            return "unknown";
        }
        return buildUrl;
    }

    private static String uploadAction(AbstractBuild<?, ?> build, FilePath uploadFile, BuildListener listener, ParamBean paramBean) {
        HttpCall call = buildHttp(paramBean, UPLOAD_URL)
                .setBodyPara(buildFilePart(uploadFile))
                .stepRate(0.05)    // 设置每发送 1% 执行一次进度回调（不设置以 StepBytes 为准）
                .setOnProcess(process -> Logging.logging(listener, Messages.UploadBuilder_Upload_progress() + (int) (process.getRate() * 100) + " %"))
                .setOnException(e -> {
                    Logging.logging(listener, Messages.UploadBuilder_Upload_exception());
                    Logging.logging(listener, e.fillInStackTrace().toString());
                })
                .post();


        if (call.getResult().isSuccessful()) {
            HttpResult<String> httpResult = call.getResult().getBody().toBean(new TypeRef<HttpResult<String>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
            Logging.logging(listener, Messages.UploadBuilder_Upload_result() + httpResult.getData());
            build.addAction(new PublishEnvVarAction("appURL", httpResult.getData()));

            return httpResult.getData();
        }
        Logging.logging(listener, Messages.UploadBuilder_Upload_fail());
        Logging.logging(listener, call.getResult().toString());
        return null;
    }

    private static RequestBody buildFilePart(FilePath filePath) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("type", "packageFiles");
        builder.addFormDataPart("file", filePath.getName(),new RequestBody(){
            @Override
            public long contentLength() throws IOException {
                try {
                    return filePath.length();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return -1;
            }

            @Nullable
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/octet-stream");
            }

            @Override
            public void writeTo(BufferedSink bufferedSink) throws IOException {
                try {
                    try (Source source = Okio.source(filePath.read())) {
                        bufferedSink.writeAll(source);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        return builder.build();
    }

    private static void savePackageInfo(ParamBean paramBean, BuildListener listener, String name,
                                        String url, String platform, String branch, String buildUrl) {

        PackageBean packageBean = new PackageBean();
        packageBean.setPkgName(name);
        packageBean.setUrl(url);
        packageBean.setPlatform(platform);
        packageBean.setProjectId(Integer.parseInt(paramBean.getProjectId()));
        packageBean.setBranch(branch);
        packageBean.setBuildUrl(buildUrl);

        com.ejlchina.okhttps.HttpResult result = http.sync(paramBean.getHost() + PACKAGE_URL)
                .addHeader(SonicToken, Secret.toString(paramBean.getApiKey()))
                .setBodyPara(packageBean)
                .put();


        if (result.isSuccessful()) {
            HttpResult<String> httpResult = result.getBody().toBean(new TypeRef<HttpResult<String>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
            Logging.logging(listener, Messages.UploadBuilder_Package_success() + httpResult.getCode());
        } else {
            Logging.logging(listener, Messages.UploadBuilder_Package_fail());
            Logging.logging(listener, result.toString());

        }

    }

    private static void runSuite(ParamBean paramBean, BuildListener listener, int suiteId) {
        com.ejlchina.okhttps.HttpResult result = http.sync(paramBean.getHost() + RUN_SUITE_URL)
                .addHeader(SonicToken, Secret.toString(paramBean.getApiKey()))
                .addUrlPara("id", suiteId)
                .get();
        Logging.logging(listener, Messages.UploadBuilder_Suite_tips() + suiteId);

        if (result.isSuccessful()) {
            HttpResult<String> httpResult = result.getBody().toBean(new TypeRef<HttpResult<String>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
            if (httpResult.getCode() == 2000 || httpResult.getCode() == 3003) {
                Logging.logging(listener, Messages.UploadBuilder_Suite_success() + httpResult);
            } else {
                Logging.logging(listener, Messages.UploadBuilder_Suite_fail() + httpResult);
            }
        } else {
            Logging.logging(listener, Messages.UploadBuilder_Suite_fail());
            Logging.logging(listener, result.toString());
        }
    }

    private static AHttpTask buildHttp(ParamBean paramBean, String uri) {
        return http.async(paramBean.getHost() + uri)
                .addHeader(SonicToken, Secret.toString(paramBean.getApiKey()));
    }

    public static HttpResult<List<Project>> listProject() {
        String host = SonicGlobalConfiguration.get().getHost();

        if (host == null) {
            return null;
        }
        return http.sync(host + PROJECT_URL)
                .get()
                .getBody()
                .toBean(new TypeRef<HttpResult<List<Project>>>() {
                    @Override
                    public Type getType() {
                        return super.getType();
                    }
                });

    }


    public static FilePath findFile(FilePath workspace, String scandir, BuildListener listener) {
        FilePath dir = null;
        if (StringUtils.hasText(scandir)) {
            dir = new FilePath(workspace, scandir);
        }else {
            dir = workspace;
        }
        Logging.logging(listener, Messages.UploadBuilder_Scan_dir() + dir);
        FilePath[] uploadFiles = null;
        try {
            if (!dir.exists() || !dir.isDirectory()) {
                Logging.logging(listener, Messages.UploadBuilder_Scan_error());
                return null;
            }

            uploadFiles = dir.list("**/*.apk,**/*.ipa");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        if (uploadFiles == null || uploadFiles.length == 0) {
            return null;
        }
        if (uploadFiles.length == 1) {
            return uploadFiles[0];
        }

        List<FilePath> strings = Arrays.asList(uploadFiles);
        FilePath finalDir = dir;
        Collections.sort(strings, (o1, o2) -> {

            try {
                return Long.compare(o1.lastModified(), o2.lastModified());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 0;
        });

        String uploadFiltPath = strings.get(0).getRemote();
        Logging.logging(listener, "Found " + uploadFiles.length + " files, the default choice of the latest modified file!");
        Logging.logging(listener, "The latest modified file is " + uploadFiltPath);
        return strings.get(0);
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
