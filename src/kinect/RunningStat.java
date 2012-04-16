/*
 * NOTE: Code copied from
 * 			 http://whileonefork.blogspot.com/2011/09/tracking-running-standard-deviation.html
 * 		All credit goes to the author Rod
 */

package kinect;

public class RunningStat {  
    private int m_n;  
    private double m_oldM;  
    private double m_newM;  
    private double m_oldS;  
    private double m_newS;  
      
    private Object m_lock = new Object();  
      
    public RunningStat() {  
        m_n = 0;  
    }  
      
    public void clear() {  
        synchronized(m_lock) {  
            m_n = 0;  
        }  
    }  
      
    public void addSample(double sample) {  
        synchronized(m_lock) {  
            m_n++;  
      
            // See Knuth TAOCP vol 2, 3rd edition, page 232  
            if (m_n == 1)  
            {  
                m_oldM = m_newM = sample;  
                m_oldS = 0.0;  
            }  
            else  
            {  
                m_newM = m_oldM + (sample - m_oldM)/m_n;  
                m_newS = m_oldS + (sample - m_oldM)*(sample - m_newM);  
      
                // set up for next iteration  
                m_oldM = m_newM;   
                m_oldS = m_newS;  
            }  
        }  
    }  
      
    public int getNumSamples() { synchronized(m_lock) { return m_n; } }  
    public double getMean() { synchronized(m_lock) { return (m_n > 0) ? m_newM : 0.0; } }  
    public double getVariance() { synchronized(m_lock) { return ( (m_n > 1) ? m_newS/(m_n - 1) : 0.0 ); } }  
    public double getStdDev() { synchronized(m_lock) { return Math.sqrt(getVariance()); } }   
}  