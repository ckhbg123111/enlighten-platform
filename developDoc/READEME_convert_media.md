# 转公众号图文及模板套用

## 需求
- 新增一个接口 在MediaConvertController
- 接口的功能是 传入一段文章，和选定的模板，返回套用模板后的html
- 入参是String essay; + 模板id， 返回是String html;
- 模板所在表 typesetting_templates
- 步骤
  - 1. 解析文章，调用第三方接口，获取文章的结构化数据
  - 2. 根据模板id，获取模板
  - 3. 将结构化数据套用到模板中，生成最终的html
- 文章解析接口
  - 参考 AI 科普平台 API 文档 v1.12（0918） 第一章 第10节 10. 获取文章结构（转换为公众号图文）/get_article_structure


## 要求
- 接口要求鉴权
- 请你充分理解需求，然后做合理的设计，再实现代码
- 代码尽可能低耦合
- 代码风格和现有代码保持统一
- 做好异常处理


## 模板替换参考：

每个模版元素里面都有个{PLACEHOLDER}的占位符，把实际的内容替换进去就可以了。双行标题的话 多一个{PLACEHOLDER1}的占位符

### 模板字段解释
- "text":  普通文本样式
- "image":  图片样式
- "single_title": 单行标题
- "double_title": 双行标题
- "text_card":  文本框
- "block_card": 图文框
- "numbered_title"：带数字标题

### 模板结构
```json
{
  "id": 1,
  "name": "template_1",
  "tag": null,
  "sort": 1,
  "header": "<p style=\"text-align: center; visibility: visible;\"><img class=\"rich_pages wxw-img\" data-galleryid=\"\" data-imgfileid=\"503453050\" data-ratio=\"0.159\" data-s=\"300,640\" style=\"height: auto !important; visibility: visible !important; width: 677px !important;\" data-original-style=\"\" data-index=\"1\" src=\"{PLACEHOLDER}\" _width=\"677px\" alt=\"图片\" data-report-img-idx=\"2\" data-fail=\"0\"></p>",
  "footer": "<p style=\"text-align: center;\"><img class=\"rich_pages wxw-img\" data-galleryid=\"\" data-imgfileid=\"503452183\" data-ratio=\"0.43796296296296294\" data-s=\"300,640\" data-type=\"jpeg\" data-w=\"1080\" style=\"height: auto !important; visibility: visible !important; width: 677px !important;\" data-original-style=\"\" data-index=\"17\" src=\"{PLACEHOLDER}\" _width=\"677px\" alt=\"图片\" data-report-img-idx=\"16\" data-fail=\"0\"></p><section data-tools=\"135编辑器\" data-id=\"131804\" style=\"margin-bottom: 0px;\"><section style=\"text-align: right;\"><section style=\"display: inline-block;\"><section style=\"display: flex;align-items: flex-end;\"><section style=\"display: flex;z-index: 9;transform: translateX(65px);-webkit-transform: translateX(65px);-moz-transform: translateX(65px);-o-transform: translateX(65px);\"><section style=\"width: 55px;margin-bottom: 12px;\"><img class=\"rich_pages wxw-img __bg_gif\" data-imgfileid=\"503454138\" data-ratio=\"1\" data-src=\"https://mmbiz.qpic.cn/sz_mmbiz_gif/o47NALMjEA6CeBLFlydfP8K9DvdqyOw2vWlOQYRrr53XxEk4vzWUl9DUymX0YzzZbRthQMmia9Y9Qp84vePdhpg/640?wx_fmt=gif&amp;from=appmsg&amp;randomid=aflmg9g9\" data-type=\"gif\" data-w=\"640\" data-width=\"100%\" style=\"width: 55px !important; display: block; vertical-align: baseline; height: auto !important; visibility: visible !important;\" data-original-style=\"width: 100%;display: block;vertical-align: baseline;\" data-index=\"18\" src=\"https://mmbiz.qpic.cn/sz_mmbiz_gif/o47NALMjEA6CeBLFlydfP8K9DvdqyOw2vWlOQYRrr53XxEk4vzWUl9DUymX0YzzZbRthQMmia9Y9Qp84vePdhpg/640?wx_fmt=gif&amp;from=appmsg&amp;randomid=aflmg9g9&amp;tp=webp&amp;wxfrom=10005&amp;wx_lazy=1\" _width=\"100%\" data-order=\"0\" alt=\"图片\" data-report-img-idx=\"17\" data-fail=\"0\"></section></section><section><section style=\"background-color: rgb(255, 246, 217);border-radius: 25px;padding: 1px 15px 1px 55px;\" class=\"js_darkmode__42\"><span style=\"font-size: 14px;text-align: justify;color: #333333;\"><strong data-brushtype=\"text\">点个「赞」和「在看」再走吧</strong></span></section><section style=\"display: flex;justify-content: flex-end;padding-right: 2em;\"><section style=\"width: 0px;height: 1px;border-left: 6px solid transparent;border-right: 6px solid transparent;border-top: 12px solid rgb(255, 246, 217);\" class=\"js_darkmode__43\"><br></section></section></section></section></section></section></section>",
  "text": "<section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">{PLACEHOLDER}</span></section>",
  "image": "<p style=\"text-align: center;\">{PLACEHOLDER}</p>",
  "singleTitle": "<section data-role=\"title\" data-tools=\"135编辑器\" data-id=\"158694\"><section style=\"margin: 10px auto;display: flex;justify-content: center;\"><section><section style=\"width: 26px;margin-bottom: -7px;margin-left: -14px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 135.21 135.21\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M67.6,0l2.25,27.81C72.09,47,88.22,63.12,107.4,65.36l27.81,2.24L107.4,69.85C88.22,72.09,72.09,88.22,69.85,107.4L67.6,135.21,65.36,107.4C63.12,88.22,47,72.09,27.81,69.85L0,67.6l27.81-2.24C47,63.12,63.12,47,65.36,27.81Z\" style=\"fill:#ffd220;\"></path></g></g></svg></section><section style=\"display: flex;flex-direction: column;\"><section style=\"background-color: rgb(30, 155, 232);padding: 9px 14px;z-index: 4;transform: skew(-15deg);\"><section style=\"transform: skew(15deg);-webkit-transform: skew(15deg);-moz-transform: skew(15deg);-o-transform: skew(15deg);\"><section style=\"font-size: 16px;color: #ffffff;text-align: center;\"><strong data-brushtype=\"text\">{PLACEHOLDER}</strong></section></section></section><section style=\"width: 100%;height: 25px;background-image: linear-gradient(to right, transparent, rgb(182, 228, 253));background-position: initial;background-size: initial;background-repeat: initial;background-attachment: initial;background-origin: initial;background-clip: initial;margin-top: -18px;z-index: 1;transform: skew(20deg);\" data-width=\"100%\"><br></section></section></section></section></section>",
  "doubleTitle": "<section data-role=\"title\" data-id=\"141293\"><section style=\"margin: 10px auto;display: flex;justify-content: center;padding-left: 30px;\"><section><section style=\"display: flex;justify-content: flex-start;\"><section style=\"margin-bottom: -5px;transform: translateX(-30px);-webkit-transform: translateX(-30px);-moz-transform: translateX(-30px);-o-transform: translateX(-30px);\"><section style=\"background-color: rgb(255, 206, 89);border-radius: 25px;padding: 2px 15px;\" class=\"js_darkmode__14\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">{PLACEHOLDER}</strong></section></section><section style=\"width: 0px;height: 1px;border-left: 8px solid transparent;border-top: 8px solid rgb(255, 206, 89);margin-left: 20px;\" class=\"js_darkmode__15\"><br></section></section></section><section style=\"display: flex;justify-content: center;\"><section style=\"display: flex;justify-content: space-between;\"><section style=\"padding: 2px 10px;background-color: rgb(30, 155, 232);border-radius: 5px 0px 0px 5px;display: flex;justify-content: space-between;align-items: center;\" class=\"js_darkmode__16\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">{PLACEHOLDER2}</strong></section></section><section style=\"flex-shrink: 0;\"><section style=\"display: flex;justify-content: space-between;align-items: flex-start;flex-direction: column;\"><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section></section></section></section></section></section></section></section>",
  "textCard": "<section style=\"visibility: visible;\" data-id=\"155504\"><section style=\"margin: 10px auto; visibility: visible;\"><section style=\"width: 25px; margin-left: 15px; margin-bottom: -13px; transform: rotateZ(0deg); visibility: visible;\"><img data-imgfileid=\"503454082\" data-ratio=\"1\" data-src=\"https://mmbiz.qpic.cn/sz_mmbiz_png/o47NALMjEA6CeBLFlydfP8K9DvdqyOw2Gchta1njqpk4Uu1to7bCGl718atVicDzGnicCHIrcDynDp8KbvKiasRibg/640?wx_fmt=png&amp;from=appmsg&amp;randomid=t0vm18cv\" data-type=\"png\" data-w=\"41\" data-width=\"100%\" style=\"width: 25px !important; display: block; vertical-align: baseline; height: auto !important; visibility: visible !important;\" data-original-style=\"width: 100%;display: block;vertical-align: baseline;\" data-index=\"3\" src=\"https://mmbiz.qpic.cn/sz_mmbiz_png/o47NALMjEA6CeBLFlydfP8K9DvdqyOw2Gchta1njqpk4Uu1to7bCGl718atVicDzGnicCHIrcDynDp8KbvKiasRibg/640?wx_fmt=png&amp;from=appmsg&amp;randomid=t0vm18cv&amp;tp=webp&amp;wxfrom=10005&amp;wx_lazy=1\" class=\"\" _width=\"100%\" alt=\"图片\" data-report-img-idx=\"0\" data-fail=\"0\"></section><section style=\"border-width: 1px; border-style: solid; border-color: rgb(89, 195, 249); border-radius: 10px; padding: 15px; visibility: visible;\" class=\"js_darkmode__0\"><section data-autoskip=\"1\" style=\"line-height: 1.75em; letter-spacing: 1.5px; font-size: 14px; color: rgb(51, 51, 51); padding-top: 5px; visibility: visible;\" class=\"js_darkmode__1\">{PLACEHOLDER}</section></section><section style=\"width: 25px;margin-left: auto;margin-top: -14px;margin-right: 20px;transform: rotateZ(0deg);\"><img data-imgfileid=\"503454083\" data-ratio=\"1\" data-src=\"https://mmbiz.qpic.cn/sz_mmbiz_png/o47NALMjEA6CeBLFlydfP8K9DvdqyOw2Gchta1njqpk4Uu1to7bCGl718atVicDzGnicCHIrcDynDp8KbvKiasRibg/640?wx_fmt=png&amp;from=appmsg&amp;randomid=30zzm4s0\" data-type=\"png\" data-w=\"41\" data-width=\"100%\" style=\"width: 25px !important; display: block; vertical-align: baseline; height: auto !important; visibility: visible !important;\" data-original-style=\"width: 100%;display: block;vertical-align: baseline;\" data-index=\"7\" src=\"https://mmbiz.qpic.cn/sz_mmbiz_png/o47NALMjEA6CeBLFlydfP8K9DvdqyOw2Gchta1njqpk4Uu1to7bCGl718atVicDzGnicCHIrcDynDp8KbvKiasRibg/640?wx_fmt=png&amp;from=appmsg&amp;randomid=30zzm4s0&amp;tp=webp&amp;wxfrom=10005&amp;wx_lazy=1\" class=\"\" _width=\"100%\" alt=\"图片\" data-report-img-idx=\"6\" data-fail=\"0\"></section></section></section>",
  "blockCard": "<section data-id=\"152673\"><section style=\"margin: 10px auto;background-color: rgb(182, 228, 253);border-radius: 10px;padding: 10px;\" class=\"js_darkmode__5\"><section style=\"border-width: 1px;border-style: dashed;border-color: rgb(90, 154, 242);border-radius: 10px;background-color: rgb(255, 255, 255);padding: 13px 10px;\" class=\"js_darkmode__6\">{PLACEHOLDER}</section></section></section>",
  "numberedTitle": "<section style=\"margin: 10px auto;text-align: left;\" data-id=\"105887\" data-tools=\"135编辑器\"><section data-brushtype=\"text\" style=\"font-size: 16px;letter-spacing: 1.5px;color: rgb(62, 157, 215);margin-bottom: -22px;margin-left: 16px;\">{PLACEHOLDER}</section><section style=\"display: flex;justify-content: center;align-items: flex-end;\"><section style=\"width: 5px;height: 22px;background-color: rgb(66, 163, 225);\" class=\"js_darkmode__8\"><br></section><section style=\"width: 100%;height: 3px;background-color0: rgb(255, 238, 194);flex: 1 1 0%;margin-left: 8px;\" data-width=\"100%\" class=\"js_darkmode__9\"><br></section></section></section>"
}

```




### 第三方返回示例

```json
{
  "code": 200,
  "success": true,
  "msg": "ok",
  "data": {
    "title": "百日咳的诊断与治疗全攻略",
    "introduction": {
      "text": "百日咳，听上去像是“咳一百天”，其实它还真不是夸张。这个病名来源于其漫长的病程，咳嗽可能持续数周甚至数月。百日咳是一种由百日咳杆菌引起的急性呼吸道传染病，尤其在儿童中高发，但成人也不能掉以轻心。今天我们就来聊聊百日咳的诊断与治疗，帮你科学应对这场“持久战”。"
    },
    "sections": [
      {
        "section_title": "典型临床表现",
        "section_paragraphs": [
          {
            "paragraph_title": "1. 卡他期（初期）",
            "paragraph_text": "持续约1～2周，症状类似普通感冒，包括流涕、打喷嚏、轻咳、低热等，容易被忽视或误诊。",
            "image_url": null
          },
          {
            "paragraph_title": "2. 痉咳期（高峰期）",
            "paragraph_text": "通常在发病后1～2周进入此阶段，持续2～6周甚至更久。典型表现为阵发性、痉挛性咳嗽，咳嗽剧烈且频繁，常伴有“鸡鸣样”吸气声，咳后呕吐也较常见。儿童尤其容易出现这种特征性表现。",
            "image_url": null
          },
          {
            "paragraph_title": "3. 恢复期（缓解期）",
            "paragraph_text": "咳嗽频率和强度逐渐减轻，但仍可能持续数周，遇冷、烟雾等刺激时容易复发。",
            "image_url": null
          }
        ]
      },
      {
        "section_title": "实验室诊断方法",
        "section_paragraphs": [
          {
            "paragraph_title": "1. 鼻咽拭子检测",
            "paragraph_text": "通过PCR技术检测百日咳杆菌的DNA，敏感性高、出结果快，是早期诊断的首选方法。",
            "image_url": null
          },
          {
            "paragraph_title": "2. 血清学检查",
            "paragraph_text": "检测特异性抗体水平，适用于病程中后期或无法采集呼吸道样本的患者。",
            "image_url": null
          },
          {
            "paragraph_title": "3. 细菌培养",
            "paragraph_text": "虽然特异性高，但对技术和时间要求较高，临床应用相对较少。",
            "image_url": null
          }
        ]
      },
      {
        "section_title": "治疗方案",
        "section_paragraphs": [
          {
            "paragraph_title": "1. 药物治疗",
            "paragraph_text": "百日咳一旦确诊，应尽早开始抗菌治疗，以缩短病程并减少传染性。首选药物：大环内酯类抗生素（如阿奇霉素、克拉霉素、红霉素）特别适用于儿童，尤其是婴幼儿。替代方案：四环素类（如多西环素）适用于8岁以上儿童及成人；喹诺酮类（如左氧氟沙星）适用于18岁以上成人。家庭成员预防性用药：密切接触者即使无症状，也建议预防性使用抗生素，防止传播。",
            "image_url": null
          },
          {
            "paragraph_title": "2. 对症治疗与护理",
            "paragraph_text": "止咳治疗：目前尚无特效止咳药，但可使用镇咳剂缓解症状，尤其在夜间影响睡眠时。止吐与营养支持：咳嗽剧烈导致呕吐者，可适当使用止吐药，并注意补充水分和营养。环境管理：保持空气流通，避免烟雾、灰尘等刺激物，饮食宜清淡易消化。休息与观察：尤其婴幼儿需密切观察是否有呼吸暂停、缺氧等严重并发症。",
            "image_url": null
          }
        ]
      },
      {
        "section_title": "不同人群治疗建议",
        "section_paragraphs": [
          {
            "paragraph_title": "儿童（特别是婴幼儿）",
            "paragraph_text": "抗生素首选红霉素或阿奇霉素，需严格遵医嘱调整剂量。婴幼儿病情进展快，建议早期住院治疗。家庭护理要特别注意防止呛咳引发窒息。",
            "image_url": null
          },
          {
            "paragraph_title": "成人",
            "paragraph_text": "成人症状相对较轻，但仍可能成为传染源。可使用多西环素或左氧氟沙星治疗。注意避免传染给儿童，尤其是未接种疫苗的婴幼儿。",
            "image_url": null
          }
        ]
      },
      {
        "section_title": "康复注意事项",
        "section_paragraphs": [
          {
            "paragraph_title": null,
            "paragraph_text": "坚持完成疗程：即使症状减轻，也应按疗程服药，防止复发或耐药。隔离防护：治疗期间应避免与婴幼儿密切接触，减少传播风险。接种疫苗是关键：百日咳疫苗（如百白破疫苗）是预防该病最有效手段，儿童应按时接种，成人可考虑加强免疫。增强免疫力：保持良好作息、均衡饮食和适度锻炼，有助于加快康复。",
            "image_url": null
          }
        ]
      }
    ],
    "summary": "百日咳虽“咳”不容缓，但只要早诊断、早治疗，配合科学护理，绝大多数患者都能顺利康复。别让“百日咳”真的咳上一百天，及时就医才是正解。"
  }
}
```