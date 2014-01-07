/*
 * This file is part of jDiffChaser.
 *
 *  jDiffChaser is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  jDiffChaser is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with jDiffChaser; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.jdiffchaser.testing;

import org.jdiffchaser.conf.Arg;

public class AppVersion {

    private String host;
    private int port = -1;
    private Arg[] args;

    public AppVersion(String host, int port, Arg[] args) {
        this.host = host;
        this.port = port;
        this.args = args;
    }

    public Arg[] getArgs() {
        return args;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
