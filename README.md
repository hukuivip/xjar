# **XJar** [![](https://jitpack.io/v/core-lib/xjar.svg)](https://jitpack.io/#core-lib/xjar)
GitHub: https://github.com/core-lib/xjar
### Spring Boot JAR 安全加密运行工具，同时支持的原生JAR。
### 基于对JAR包内资源的加密以及拓展ClassLoader来构建的一套程序加密启动，动态解密运行的方案，避免源码泄露或反编译。

## **功能特性**
* 无需侵入代码，只需要把编译好的JAR包通过工具加密即可。
* 完全内存解密，杜绝源码以及字节码泄露或反编译。
* 支持所有JDK内置加解密算法。
* 可选择需要加解密的字节码或其他资源文件，避免计算资源浪费。

## **环境依赖**
JDK 1.7 +

## **使用步骤**

```xml
<project>
    <!-- 设置 jitpack.io 仓库 -->
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://www.jitpack.io</url>
        </repository>
    </repositories>
    <!-- 添加 XJar 依赖 -->
    <dependencies>
        <dependency>
            <groupId>com.github.core-lib</groupId>
            <artifactId>xjar</artifactId>
            <version>LATEST_VERSION</version>
        </dependency>
    </dependencies>
</project>
```

```java
// Spring-Boot Jar包加密
public static void main(String[] args) {
    String password = "io.xjar";
    File plaintext = new File("/path/to/read/plaintext.jar");
    File encrypted = new File("/path/to/save/encrypted.jar");
    XBoot.encrypt(plaintext, encrypted, password);
}
```

```java
// Spring-Boot Jar包解密
public static void main(String[] args) {
    String password = "io.xjar";
    File encrypted = new File("/path/to/read/encrypted.jar");
    File decrypted = new File("/path/to/save/decrypted.jar");
    XBoot.decrypt(encrypted, decrypted, password);
}
```

```java
// Jar包加密
public static void main(String[] args) {
    String password = "io.xjar";
    File plaintext = new File("/path/to/read/plaintext.jar");
    File encrypted = new File("/path/to/save/encrypted.jar");
    XJar.encrypt(plaintext, encrypted, password);
}
```

```java
// Jar包解密
public static void main(String[] args) {
    String password = "io.xjar";
    File encrypted = new File("/path/to/read/encrypted.jar");
    File decrypted = new File("/path/to/save/decrypted.jar");
    XJar.decrypt(encrypted, decrypted, password);
}
```

```text
// 命令行运行JAR 然后在提示输入密码的时候输入密码后按回车即可正常启动
java -jar /path/to/encrypted.jar
//也可以通过传参的方式直接启动
java -jar /path/to/encrypted.jar --xjar.password=PASSWORD
```

## **参数说明**
* --xjar.algorithm  加解密算法名称，缺省为AES，支持JDK所有内置算法，如AES / DES ...
* --xjar.keysize    密钥长度，缺省为128，根据不同的算法选取不同的密钥长度。
* --xjar.ivsize     向量长度，缺省为128，根据不同的算法选取不同的向量长度。
* --xjar.password   密码

## **进阶用法**
```java
// 只加密自身项目及相关模块的源码不加密第三方依赖，可以通过XJarArchiveEntryFilter来定制需要加密的JAR包内资源
public static void main(String[] args) {
    String password = "io.xjar";
    File plaintext = new File("/path/to/read/plaintext.jar");
    File encrypted = new File("/path/to/save/encrypted.jar");
    XBoot.encrypt(plaintext, encrypted, password, new XJarArchiveEntryFilter() {
        @Override
        public boolean filter(JarArchiveEntry entry) {
            return entry.getName().startsWith("/BOOT-INF/classes/")
             || entry.getName().startsWith("/BOOT-INF/lib/jar-need-encrypted");
        }
    });
}
```

## 变更记录
* v1.0.6
    1. 采用[LoadKit](https://github.com/core-lib/loadkit)作为资源加载工具
* v1.0.5
    1. 支持并行类加载，需要JDK1.7+的支持，可提升多线程环境类加载的效率
    2. Spring-Boot JAR 包加解密增加一个安全过滤器，避免无关资源被加密造成无法运行
    3. XBoot / XJar 工具类中增加多个按文件路径加解密的方法，提升使用便捷性
* v1.0.4 小优化
* v1.0.3 增加Spring-Boot的FatJar加解密时的缺省过滤器，避免由于没有提供过滤器时加密后的JAR包不能正常运行。
* v1.0.2 修复中文及空格路径的问题
* v1.0.1 升级detector框架
* v1.0.0 第一个正式版发布

## 协议声明
[Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0)

## 联系作者
QQ 646742615 不会钓鱼的兔子
