package org.wangyl;
import java.io.File;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author wangyl
 * 清除静态缓存控制入口
 * @date  2018-5-16
 */
@Mojo(name="cache",defaultPhase=LifecyclePhase.PACKAGE)
public class CacheMojo extends AbstractMojo {

    /**
     * 默认值
     */
    private String defaultVersion = UUID.randomUUID().toString();
    private String defaultBaseDir = "src" + File.separator + "main" + File.separator +"webapp";
    private String defaultSuffixs ="html,htm";

    /**
     * 版本号
     */
    @Parameter
    private String edition;
    /**
     * 静态资源文件包
     */
    @Parameter
    private String baseDir;
    /**
     * 后缀匹配
     */
    @Parameter
    private String[] sufffixs;

    private List<File> staticFiles = new ArrayList<File>();

    private Pattern IMAGES_PATTERN = Pattern.compile(
            "<img[\\s\\S]+?src\\s*=\\s*[\"|\']\\s*(.+\\.(png|jpg|gif|bmp|jpeg)+.*?)[\"|\']{1}",
            Pattern.CASE_INSENSITIVE);

    private Pattern JS_PATTERN = Pattern.compile(
            "<script[\\s\\S]+?src\\s*=\\s*[\"|\'](.+\\.js.*?)[\"|\']{1}", Pattern.CASE_INSENSITIVE);

    private Pattern CSS_PATTERN = Pattern.compile(
            "<link[\\s\\S]+?href\\s*=\\s*[\"|\'](.+\\.css.*?)[\"|\']{1}", Pattern.CASE_INSENSITIVE);

    public void execute() {
        setDefaultParams();
        getAllFiles(baseDir);
        handleFile();
    }

    private void setDefaultParams(){
        if(StringUtils.isEmpty(edition)){
            edition = defaultVersion;
        }

        if(StringUtils.isEmpty(baseDir)){
            baseDir = defaultBaseDir;
        }else{
            baseDir = baseDir + File.separator +defaultBaseDir;
        }
        if(ArrayUtils.isEmpty(sufffixs)){
            sufffixs = defaultSuffixs.split(",");
        }
    }

    private void getAllFiles(String baseDir){
        File file = new File(baseDir);
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        List<String> standard = Arrays.asList(sufffixs);
        // 遍历，目录下的所有文件
        for (File f : files) {
            if (f.isFile()) {
                String sufix = FilenameUtils.getExtension(f.getName());
                if(standard.contains(sufix) && !staticFiles.contains(f.getAbsoluteFile())){
                    staticFiles.add(f.getAbsoluteFile());
                }
            } else if (f.isDirectory()) {
                getAllFiles(f.getAbsolutePath());
            }
        }
    }

    private void handleFile() {
        try{
            for(File file :staticFiles){
                String pageText = FileUtils.readFileToString(file, "UTF-8");
                pageText = doHandle(pageText);
                FileUtils.writeStringToFile(file,pageText,"UTF-8");
            }
        }catch (Exception e){
            System.out.print(e.toString());
        }
    }

    private String doHandle(String pageText){
        pageText = handleJSFile(pageText);
        pageText = handleCSSFile(pageText);
        pageText = handleImageFile(pageText);
        return pageText;
    }

    private String handleJSFile(String pageText){
        Matcher matcher = JS_PATTERN.matcher(pageText);
        StringBuffer sb = new StringBuffer();
        doMatch(matcher,sb);
        return sb.toString();
    }
    private String handleCSSFile(String pageText){
        Matcher matcher = CSS_PATTERN.matcher(pageText);
        StringBuffer sb = new StringBuffer();
        doMatch(matcher,sb);
        return sb.toString();
    }

    private String handleImageFile(String pageText){
        Matcher matcher = IMAGES_PATTERN.matcher(pageText);
        StringBuffer sb = new StringBuffer();
        doMatch(matcher,sb);
        return sb.toString();
    }

    private void doMatch(Matcher matcher,StringBuffer sb){
        while(true){
            if(!matcher.find()){
                break;
            }
            String pfull = matcher.group();
            String jsPart = matcher.group(1);
            pfull = pfull.substring(0, matcher.start(1) - matcher.start()) + versionStaticUrl(jsPart);
            matcher.appendReplacement(sb,Matcher.quoteReplacement(pfull)+"\"");
        }
        matcher.appendTail(sb);
    }

    private String versionStaticUrl(String content){
        if(content.contains("?v=")){
            content = content.substring(0,content.indexOf("?v="));
        }
        return content.trim() + "?"+ "v" + "=" + edition;
    }
}
