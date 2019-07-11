package com.runhang.shadow.server.common.utils;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * @ClassName StringJavaObject
 * @Description 
 * @Date 2019/7/11 19:46
 * @author szh
 **/
public class StringJavaObject extends SimpleJavaFileObject {

    // 源代码
    private String content;

    // 遵循Java规范的类名及文件
    StringJavaObject(String _javaFileName, String _content) {
        super(_createStringJavaObjectUri(_javaFileName), JavaFileObject.Kind.SOURCE);
        content = _content;
    }

    // 产生一个URL资源库
    private static URI _createStringJavaObjectUri(String name) {
        // 注意此处没有设置包名
        return URI.create("String:///" + name + JavaFileObject.Kind.SOURCE.extension);
    }

    // 文本文件代码
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }

}
