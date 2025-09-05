package com.zhao.easyJmeter.common.jmeter.component;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.control.LoopController;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 线程组组件实现
 */
public class ThreadGroupComponent extends JMeterComponentBase {

    public ThreadGroupComponent() {
        this.setComponentType("ThreadGroup");
        this.setMetadata(createMetadata());
        this.setProperties(getDefaultProperties());
    }

    @Override
    public String getTestElementClass() {
        return "org.apache.jmeter.threads.ThreadGroup";
    }

    @Override
    public String getGuiClass() {
        return "org.apache.jmeter.threads.gui.ThreadGroupGui";
    }

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();

        // 验证线程数 (Required)
        Object numThreadsObj = getProperties().get("numThreads");
        if (numThreadsObj == null || !StringUtils.hasText(numThreadsObj.toString())) {
            result.addError("numThreads", "线程数不能为空");
        } else {
            try {
                if (Integer.parseInt(numThreadsObj.toString()) <= 0) {
                    result.addError("numThreads", "线程数必须大于0");
                }
            } catch (NumberFormatException e) {
                result.addError("numThreads", "线程数必须是有效的整数");
            }
        }

        // 验证循环次数 (Required)
        Object loopsObj = getProperties().get("loops");
        if (loopsObj == null || !StringUtils.hasText(loopsObj.toString())) {
            result.addError("loops", "循环次数不能为空");
        } else {
            try {
                if (Integer.parseInt(loopsObj.toString()) < -1) {
                    result.addError("loops", "循环次数必须大于等于-1（-1表示永久循环）");
                }
            } catch (NumberFormatException e) {
                result.addError("loops", "循环次数必须是有效的整数");
            }
        }

        // 验证启动时间 (Required)
        Object rampTimeObj = getProperties().get("rampTime");
        if (rampTimeObj == null || !StringUtils.hasText(rampTimeObj.toString())) {
            result.addError("rampTime", "启动时间不能为空");
        } else {
            try {
                if (Integer.parseInt(rampTimeObj.toString()) < 0) {
                    result.addError("rampTime", "启动时间不能小于0");
                }
            } catch (NumberFormatException e) {
                result.addError("rampTime", "启动时间必须是有效的整数");
            }
        }

        return result;
    }

    @Override
    public TestElement toTestElement() {
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        threadGroup.setName(this.getName());
        threadGroup.setEnabled(this.isEnabled());

        // 假设 validate() 方法已在此之前调用，确保了属性的有效性
        // 设置线程数
        Object numThreads = getProperties().get("numThreads");
        if (numThreads != null) {
            threadGroup.setNumThreads(Integer.parseInt(numThreads.toString()));
        }

        // 设置启动时间
        Object rampTime = getProperties().get("rampTime");
        if (rampTime != null) {
            threadGroup.setRampUp(Integer.parseInt(rampTime.toString()));
        }

        // 设置循环次数
        Object loops = getProperties().get("loops");
        if (loops != null) {
            int loopCount = Integer.parseInt(loops.toString());
            LoopController loopController = new LoopController();
            if (loopCount == -1) {
                loopController.setContinueForever(true);
                loopController.setLoops(-1);
            } else {
                loopController.setContinueForever(false);
                loopController.setLoops(loopCount);
            }
            loopController.initialize();
            threadGroup.setSamplerController(loopController);
        }

        // 设置调度器
        Object scheduler = getProperties().get("scheduler");
        if (scheduler != null && Boolean.parseBoolean(scheduler.toString())) {
            threadGroup.setScheduler(true);

            Object duration = getProperties().get("duration");
            if (duration != null) {
                threadGroup.setDuration(Long.parseLong(duration.toString()));
            }

            Object delay = getProperties().get("delay");
            if (delay != null) {
                threadGroup.setDelay(Long.parseLong(delay.toString()));
            }
        }

        return threadGroup;
    }

    @Override
    public void fromTestElement(TestElement element) {
        if (!(element instanceof ThreadGroup)) {
            throw new IllegalArgumentException("Element must be a ThreadGroup");
        }

        ThreadGroup threadGroup = (ThreadGroup) element;
        this.setName(threadGroup.getName());
        this.setEnabled(threadGroup.isEnabled());

        Map<String, Object> properties = new HashMap<>();
        properties.put("numThreads", threadGroup.getNumThreads());
        properties.put("rampTime", threadGroup.getRampUp());
        if (threadGroup.getSamplerController() instanceof LoopController) {
            LoopController lc = (LoopController) threadGroup.getSamplerController();
            properties.put("loops", lc.getLoops());
        } else {
            properties.put("loops", 1);
        }
        properties.put("scheduler", threadGroup.getScheduler());
        properties.put("duration", threadGroup.getDuration());
        properties.put("delay", threadGroup.getDelay());

        this.setProperties(properties);
    }

    @Override
    public Map<String, Object> getDefaultProperties() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("numThreads", 1);
        defaults.put("rampTime", 1);
        defaults.put("loops", 1);
        defaults.put("scheduler", false);
        defaults.put("duration", 0);
        defaults.put("delay", 0);
        return defaults;
    }

    @Override
    public List<PropertyDefinition> getSupportedProperties() {
        List<PropertyDefinition> properties = new ArrayList<>();

        properties.add(PropertyDefinition.builder()
                .name("numThreads")
                .displayName("线程数")
                .description("并发执行的线程数量")
                .type(PropertyDefinition.PropertyType.INTEGER)
                .defaultValue(1)
                .required(true)
                .group("基本设置")
                .order(1)
                .build());

        properties.add(PropertyDefinition.builder()
                .name("rampTime")
                .displayName("启动时间(秒)")
                .description("所有线程启动完成所需的时间")
                .type(PropertyDefinition.PropertyType.INTEGER)
                .defaultValue(1)
                .required(true)
                .group("基本设置")
                .order(2)
                .build());

        properties.add(PropertyDefinition.builder()
                .name("loops")
                .displayName("循环次数")
                .description("每个线程执行的循环次数，-1表示永久循环")
                .type(PropertyDefinition.PropertyType.INTEGER)
                .defaultValue(1)
                .required(true)
                .group("基本设置")
                .order(3)
                .build());

        properties.add(PropertyDefinition.builder()
                .name("scheduler")
                .displayName("调度器")
                .description("是否启用调度器")
                .type(PropertyDefinition.PropertyType.BOOLEAN)
                .defaultValue(false)
                .group("调度设置")
                .order(4)
                .build());

        properties.add(PropertyDefinition.builder()
                .name("duration")
                .displayName("持续时间(秒)")
                .description("测试持续时间")
                .type(PropertyDefinition.PropertyType.LONG)
                .defaultValue(0)
                .group("调度设置")
                .order(5)
                .build());

        properties.add(PropertyDefinition.builder()
                .name("delay")
                .displayName("启动延迟(秒)")
                .description("测试开始前的延迟时间")
                .type(PropertyDefinition.PropertyType.LONG)
                .defaultValue(0)
                .group("调度设置")
                .order(6)
                .build());

        return properties;
    }

    private ComponentMetadata createMetadata() {
        return ComponentMetadata.builder()
                .displayName("线程组")
                .description("定义并发用户的数量和行为")
                .category("线程组")
                .icon("icon-thread-group")
                .container(true)
                .acceptedChildren(new String[]{"HTTPSamplerProxy", "HeaderManager", "CSVDataSet", "ResultCollector"})
                .acceptedParents(new String[]{"TestPlan"})
                .sortOrder(1)
                .builtin(true)
                .build();
    }
}