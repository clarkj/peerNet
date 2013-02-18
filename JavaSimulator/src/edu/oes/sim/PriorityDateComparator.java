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
