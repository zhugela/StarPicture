"""
生成 4 个 JMeter .jmx 性能测试脚本骨架
放到各成员文件夹的 性能测试/ 目录下
导入方法：JMeter → File → Open → 选 jmx
"""
from pathlib import Path
import uuid

BASE = Path("D:/code/StarPicture/docs/test")

def gen_jmeter(name, target, threads, ramp, loops, body, headers=None):
    """生成 JMeter 5.6 兼容的 .jmx"""
    if headers is None:
        headers = [{"name": "Content-Type", "value": "application/json"}]

    header_manager = '''<HeaderManager guiclass="HeaderPanel" testclass="HeaderManager" testname="HTTP Header Manager" enabled="true">
          <collectionProp name="HeaderManager.headers">
{items}
          </collectionProp>
        </HeaderManager>'''.format(
            items="\n".join(
                f'            <elementProp name="{h["name"]}" elementType="Header">\n'
                f'              <stringProp name="Header.name">{h["name"]}</stringProp>\n'
                f'              <stringProp name="Header.value">{h["value"]}</stringProp>\n'
                f'            </elementProp>'
                for h in headers
            )
        )

    jmx = f'''<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.6.3">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="{name}" enabled="true">
      <stringProp name="TestPlan.comments">{name} - {target}</stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="线程组" enabled="true">
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller">
          <stringProp name="LoopController.loops">{loops}</stringProp>
          <boolProp name="LoopController.continue_forever">false</boolProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">{threads}</stringProp>
        <stringProp name="ThreadGroup.ramp_time">{ramp}</stringProp>
        <boolProp name="ThreadGroup.scheduler">false</boolProp>
        <stringProp name="ThreadGroup.duration"></stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
      </ThreadGroup>
      <hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="{target}" enabled="true">
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
            <collectionProp name="Arguments.arguments">
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.value">{body}</stringProp>
                <stringProp name="HTTPArgument.use_equals_in_query">true</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>
          <stringProp name="HTTPSampler.domain">localhost</stringProp>
          <stringProp name="HTTPSampler.port">8123</stringProp>
          <stringProp name="HTTPSampler.protocol">http</stringProp>
          <stringProp name="HTTPSampler.contentEncoding">utf-8</stringProp>
          <stringProp name="HTTPSampler.path">/api{target}</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
        </HTTPSamplerProxy>
        <hashTree>
{header_manager}
          <hashTree/>
          <ResultCollector guiclass="SummaryReport" testclass="ResultCollector" testname="Summary Report" enabled="true">
            <boolProp name="ResultCollector.error_logging">false</boolProp>
            <objProp>
              <name>saveConfig</name>
              <value class="SampleSaveConfiguration">
                <time>true</time>
                <latency>true</latency>
                <timestamp>true</timestamp>
                <success>true</success>
                <label>true</label>
                <code>true</code>
                <message>true</message>
                <threadName>true</threadName>
                <dataType>true</dataType>
                <encoding>false</encoding>
                <assertions>true</assertions>
                <subresults>true</subresults>
                <responseData>false</responseData>
                <samplerData>false</samplerData>
                <xml>false</xml>
                <fieldNames>true</fieldNames>
                <responseHeaders>false</responseHeaders>
                <requestHeaders>false</requestHeaders>
                <responseDataOnError>false</responseDataOnError>
                <saveAssertionResultsFailureMessage>true</saveAssertionResultsFailureMessage>
                <assertionsResultsToSave>0</assertionsResultsToSave>
                <bytes>true</bytes>
                <sentBytes>true</sentBytes>
                <url>true</url>
                <threadCounts>true</threadCounts>
                <idleTime>true</idleTime>
                <connectTime>true</connectTime>
              </value>
            </objProp>
            <stringProp name="filename"></stringProp>
          </ResultCollector>
          <hashTree/>
        </hashTree>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
'''
    return jmx

# ============ 朱远亮：登录 50 并发 ============
jmx_zhyl = gen_jmeter(
    "登录_50并发_5秒",
    "/user/login",
    threads=50, ramp=5, loops=1,
    body='{"userAccount":"testuser01","userPassword":"12345678"}'
)

# ============ 李冠燃：图片上传 20 并发 + 分页查询 50 并发 ============
jmx_pic_upload = gen_jmeter(
    "图片上传_20并发_2MB",
    "/file/upload",
    threads=20, ramp=3, loops=1,
    body="(multipart, 请在 JMeter 改成 multipart 模式上传 2MB.jpg)",
    headers=[
        {"name": "Content-Type", "value": "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW"}
    ]
)
jmx_pic_query = gen_jmeter(
    "图片分页查询_50并发",
    "/picture/list/page",
    threads=50, ramp=5, loops=1,
    body='{"current":1,"pageSize":10}'
)

# ============ 李坤纬：空间分析 20 并发 ============
jmx_space = gen_jmeter(
    "空间分析_20并发",
    "/space/analyze/usage",
    threads=20, ramp=3, loops=1,
    body='{"spaceId":1}'
)

# ============ 林景彬：文件上传 50 并发 1MB ============
jmx_file = gen_jmeter(
    "文件上传_50并发_1MB",
    "/file/upload",
    threads=50, ramp=5, loops=1,
    body="(multipart, 请在 JMeter 改成 multipart 模式上传 1MB.jpg)",
    headers=[
        {"name": "Content-Type", "value": "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW"}
    ]
)

files = [
    (BASE / "朱远亮_脚本与截图/性能测试/login_50concurrent.jmx", jmx_zhyl),
    (BASE / "李冠燃_脚本与截图/性能测试/picture_upload_20concurrent.jmx", jmx_pic_upload),
    (BASE / "李冠燃_脚本与截图/性能测试/picture_query_50concurrent.jmx", jmx_pic_query),
    (BASE / "李坤纬_脚本与截图/性能测试/space_analyze_20concurrent.jmx", jmx_space),
    (BASE / "林景彬_脚本与截图/性能测试/file_upload_50concurrent_1MB.jmx", jmx_file),
]

for p, content in files:
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content, encoding='utf-8')
    print(f"已生成: {p}")

print(f"\n共 {len(files)} 个 JMeter 脚本。")
