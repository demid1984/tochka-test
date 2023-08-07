package ru.zdemid.tochka.properties;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PropertyBinder<T> {

    private final Class<T> propertyClass;
    private final Map<String, Field> propertyFields = new HashMap<>();
    private final Options options = new Options();

    public PropertyBinder(Class<T> propertyClass) {
        this.propertyClass = propertyClass;
        for (Field declaredField : propertyClass.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(Property.class)) {
                Property property = declaredField.getAnnotation(Property.class);
                Option option = new Option(property.option(), property.longOption(), true, property.description());
                option.setRequired(property.required());
                option.setType(declaredField.getType());
                options.addOption(option);
                propertyFields.put(property.longOption(), declaredField);
            }
        }
    }

    public T bind(String[] args) {
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = parser.parse(options, args);
            return bindProperty(cmd);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            String applicationName = System.getenv("APPLICATION_NAME");
            formatter.printHelp(applicationName == null ? "test-app" : applicationName, options);
            return null;
        } catch (ReflectiveOperationException e) {
            log.error("Internal application error. Please contact support", e);
            return null;
        }
    }

    private T bindProperty(CommandLine cmd) throws ParseException, ReflectiveOperationException {
        T property = propertyClass.getConstructor().newInstance();
        for (Option option : options.getOptions()) {
            Object value = cmd.getOptionValue(option);
            Field field = propertyFields.get(option.getLongOpt());
            field.setAccessible(true);
            if (value != null || field.get(property) == null) {
                field.set(property, value);
            }
        }
        return property;
    }

}
