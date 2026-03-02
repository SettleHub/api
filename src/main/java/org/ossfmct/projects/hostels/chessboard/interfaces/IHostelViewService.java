package org.ossfmct.projects.hostels.chessboard.interfaces;

import org.ossfmct.projects.hostels.chessboard.models.ViewHostel;
import java.io.IOException;

public interface IHostelViewService {

    ViewHostel getViewHostel(String hostelNumber) throws IOException;

}
