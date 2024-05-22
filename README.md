# notepad

**目前暂时想的框架目录**

```java
class User // 用户基础类
    String username // 用户名
    List[String] tags // 所有出现的标签，便于初始界面分类
    List[Note] notes // 所有笔记

class Note // 笔记基础类
    String name // 笔记标题
    Time start_time // 创建时间（变量类型待定？）
    Time edit_time // 最后编辑时间（变量类型待定？）
    List[String] tags // 笔记的标签
    List[Paragraph] paragraphs // 考虑以段落为单位（因为可以把图片和音频认为是一个单独的段落？）

class Paragraph // 基类
    static const int TEXT = x1 // x1待定
    static const int IMAGE = x2 // x2待定
    static const int MEDIA = x3 // x3待定
    int type // 段落类型

class TextParagraph extends Paragraph // 文本段落
    String text // 文本类段落（如何调整单个文本的字号颜色等？）
    // TODO（样式设定待定）


class ImageParagraph extends Paragraph // 图像段落
    Image image // java可能会有相应的图像类？（图像控件主题外观应该要写）
    // TODO（样式设定待定）

class MediaParagraph extends Paragraph // 文本段落
    Media media // java可能会有相应的音频类？（音频控件主题外观应该要写）
    // TODO（样式设定待定）
```

**2024.5.22 update**
- 后端部分v1.0完成