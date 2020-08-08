package com.legend.network;

import com.legend.utils.PropertiesUtils;
import com.legend.utils.StringUtils;

import javax.print.attribute.standard.NumberUp;

/**
 * @author Legend
 * @data by on 20-8-6.
 * @description
 */
public class Configuration {

    public static final String ADDRESS = "address";
    public static final String PORT = "port";

    public static final int SUCCESS = 1;
    public static final int FAILURE = -1;

    public static String SERVER_ADDRESS = "localhost";
    public static int SERVER_PORT = 8888;

    static {
        String address = PropertiesUtils.get(ADDRESS);
        if (StringUtils.isValidIP(address)) {
            SERVER_ADDRESS = address;
        }
        String port = PropertiesUtils.get(PORT);
        if (StringUtils.isHexNumeric(port)) {
            SERVER_PORT = Integer.parseInt(port);
        }
    }

}
