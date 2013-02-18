/*
Compaires messages
Copyright (C) 2012   Joshua Clark
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
 */
package edu.oes.sim;

import java.util.Comparator;

public class PriorityDateComparator implements Comparator<Message> {

	@Override
    public int compare(Message x, Message y)
    {
        // Assume neither string is null. Real code should
        // probably be more robust
        if (x.reSendTime < y.reSendTime)
        {
            return -1;
        }
        if (x.reSendTime > y.reSendTime)
        {
            return 1;
        }
        return 0;
    }
}
