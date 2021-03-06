/* Copyright 2009 Marc G. Bellemare
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.rlcommunity.critterbot.javadrops.clients;

/**
  * DiscoInterfaceClientHandler
  *
  * This class handles client handling for the Disco Interface TCP/IP server.
  */

import org.rlcommunity.critterbot.javadrops.InterfaceInputStream;
import org.rlcommunity.critterbot.javadrops.InterfaceOutputStream;
import org.rlcommunity.critterbot.javadrops.drops.*;


import java.net.Socket;

import java.util.LinkedList;

import java.io.IOException;


public class DiscoInterfaceClientHandler extends Thread
{
  public final int MAX_CLASSNAME_LENGTH = 1024;

  /** The maximum number of drops to keep in the queue */
  protected final int aMaxQueuedDrops;
  
  protected Socket aClient;

  /** List of queue'd elements waiting to be parsed by our server */
  protected final LinkedList<SimulatorDrop> aInQueue;

  protected InterfaceInputStream aIn;
  protected InterfaceOutputStream aOut;

  protected boolean aClosed = false;


  /** Creates a new client handler corresponding to the given Socket */
  public DiscoInterfaceClientHandler(Socket pClient, int pMaxQueueSize) {
    aClient = pClient;
    try
    {
      aIn = new InterfaceInputStream(aClient.getInputStream());
      aOut = new InterfaceOutputStream(aClient.getOutputStream());
    }
    catch (IOException e)
    {
      System.err.println ("Failed to create input/output streams."); 
    }

    aInQueue = new LinkedList<SimulatorDrop>();
    aMaxQueuedDrops = pMaxQueueSize;
  }

  /** Main code for this Thread */
  public void run()
  {
    while (!aClosed)
    {
      // Block and wait for new data
      // Read in a new drop (first its class name)
      try 
      {
        int nameLength = aIn.readInt();

        // If we don't test for this, we can kill the heap 
        if (nameLength > MAX_CLASSNAME_LENGTH)
        {
          throw new RuntimeException ("Garbage data");
        }

        String className = aIn.readString(nameLength);

        // Create a new Drop
        try
        {
          SimulatorDrop newDrop = DropFactory.create(className);

          // Read in the drop size
          int dropSize = aIn.readInt();
          // Read in the drop!
          newDrop.readData(aIn, dropSize);
        
          // Add the drop to the queue
          synchronized(aInQueue)
          {
            aInQueue.add(newDrop);
            // Remove old elements
            while (aInQueue.size() > aMaxQueuedDrops) {
                SimulatorDrop deadDrop = aInQueue.removeFirst();
                System.err.println ("Warning: discarding old drop: "+
                        deadDrop.getClass().getSimpleName());
            }
          }
        }
        catch (ClassNotFoundException e)
        {
          System.err.println ("Invalid drop name: "+className);
          System.err.println ("Aborting - no synchronization mechanism.");
          close();
        }
        // Catch other ugly exceptions here, and throw them as Runtime
        catch (Exception e)
        {
          throw new RuntimeException(e);
        }
      }
      catch (IOException e)
      {
        System.err.println ("IOException in DiscoInterfaceClientHandler.run - aborting.");
        System.err.println ("Type: "+e.toString());
        // End this thread and the associated socket asap; we use close()
        //  so that our owner (the server) knows to delete us
        this.close(); 
      }
    }

    // We are closed, end the socket
    try
    {
      aClient.close();
    }
    // Really, there is nothing to do if we fail to close the socket
    catch (Exception ee)
    {
    }
  }

  /** Send out a drop */ 
  public void send(SimulatorDrop pData)
  {
    synchronized(aOut) 
    {
      // Write the Drop's data to the output stream
      try
      {
        String className = pData.getClass().getSimpleName();
        aOut.writeString(className);
    
        // Write the drop size
        aOut.writeInt(pData.getSize());
        
        // Get the drop to write itself to the output stream
        pData.writeData(aOut);
      }
      catch (IOException e)
      {
        System.err.println ("IOException in DiscoInterfaceClientHandler.send :: quitting");
        throw new RuntimeException(e);
      }
    }
  }

  public SimulatorDrop receive()
  {
    // Pop a drop if there is one
    synchronized (aInQueue)
    {
      if (aInQueue.isEmpty()) return null;
      else return aInQueue.remove();
    }
  }

  /** 'Lazy' close of the socket. This will cause the socket to be closed 
    *  by the client thread.
    */
  private synchronized void close()
  {
    aClosed = true;
  }

  public boolean isClosed() { return aClosed; }
}
