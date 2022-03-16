/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * Project Info:  https://plantuml.com
 *
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */
package net.sourceforge.plantuml.servlet;

import java.io.BufferedReader;
import java.io.IOException;

import javax.imageio.IIOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.OptionFlags;
import net.sourceforge.plantuml.servlet.utility.UmlExtractor;
import net.sourceforge.plantuml.servlet.utility.UrlDataExtractor;

/**
 * Common service servlet to produce diagram from compressed UML source contained in the end part of the requested URI.
 */
@SuppressWarnings("SERIAL")
public abstract class UmlDiagramService extends HttpServlet {

    static {
        OptionFlags.ALLOW_INCLUDE = false;
        if ("true".equalsIgnoreCase(System.getenv("ALLOW_PLANTUML_INCLUDE"))) {
            OptionFlags.ALLOW_INCLUDE = true;
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final String url = request.getRequestURI();
        final String encoded = UrlDataExtractor.getEncodedDiagram(url, "");
        final int idx = UrlDataExtractor.getIndex(url, 0);

        // build the UML source from the compressed request parameter
        final String uml;
        try {
            uml = UmlExtractor.getUmlSource(encoded);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            return;
        }

        //Currently disabled to allow Confluence Servers to work.
        //CORS is set in DiagramResponse as per the original code
        /*
        String ref = request.getHeader("referer");
        String dom = null;

        if (ref != null && ref.toLowerCase()
                .matches("https?://([a-z0-9,-]+[.])*draw[.]io/.*"))
        {
            dom = ref.toLowerCase().substring(0, ref.indexOf(".draw.io/") + 8);
        }
        else if (ref != null && ref.toLowerCase()
                .matches("https?://([a-z0-9,-]+[.])*diagrams[.]net/.*"))
        {
            dom = ref.toLowerCase().substring(0, ref.indexOf(".diagrams.net/") + 13);
        }
        else if (ref != null && ref.toLowerCase()
                .matches("https?://([a-z0-9,-]+[.])*stratus-addons[.]com/.*"))
        {
            dom = ref.toLowerCase().substring(0, ref.indexOf(".stratus-addons.com/") + 19);
        }
        else if (ref != null && ref.toLowerCase()
                .matches("https?://([a-z0-9,-]+[.])*quipelements[.]com/.*"))
        {
            dom = ref.toLowerCase().substring(0, ref.indexOf(".quipelements.com/") + 17);
        }

        if (dom == null)
        {
            // response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            // DB commented out 04.04.19
            // Prevented working on Confluence Servers
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET");
        }
        else
        {
            response.addHeader("Access-Control-Allow-Origin", dom);
            response.addHeader("Access-Control-Allow-Methods", "GET");
        }
        */
        doDiagramResponse(request, response, uml, idx);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final int idx = UrlDataExtractor.getIndex(request.getRequestURI(), 0);

        // read textual diagram source from request body
        final StringBuilder uml = new StringBuilder();
        try (BufferedReader in = request.getReader()) {
            String line;
            while ((line = in.readLine()) != null) {
                uml.append(line).append('\n');
            }
        }

        doDiagramResponse(request, response, uml.toString(), idx);
    }

    /**
     * Send diagram response.
     *
     * @param request html request
     * @param response html response
     * @param uml textual UML diagram(s) source
     * @param idx diagram index of {@code uml} to send
     *
     * @throws IOException if an input or output exception occurred
     */
    private void doDiagramResponse(
        HttpServletRequest request,
        HttpServletResponse response,
        String uml,
        int idx
    ) throws IOException {
        // generate the response
        DiagramResponse dr = new DiagramResponse(response, getOutputFormat(), request);
        try {
            dr.sendDiagram(uml, idx);
        } catch (IIOException e) {
            // Browser has closed the connection, so the HTTP OutputStream is closed
            // Silently catch the exception to avoid annoying log
        }
    }

    /**
     * Gives the wished output format of the diagram. This value is used by the DiagramResponse class.
     *
     * @return the format
     */
    abstract public FileFormat getOutputFormat();

}
