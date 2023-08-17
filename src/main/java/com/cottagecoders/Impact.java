package com.cottagecoders;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.zendesk.client.v2.Zendesk;
import org.zendesk.client.v2.model.CustomFieldValue;
import org.zendesk.client.v2.model.Organization;
import org.zendesk.client.v2.model.Ticket;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Impact {

  @Parameter(names = {"-i", "--input"}, description = "path to input file.")
  private String input = "";

  public static void main(String[] args) throws FileNotFoundException {
    Impact impact = new Impact();
    impact.run(args);
    System.exit(0);

  }

  private void run(String[] args) throws FileNotFoundException {
    // process command line args.
    JCommander.newBuilder().addObject(this).build().parse(args);

    // process command line dates..
    Zendesk zd = null;
    try {
      // set up Zendesk connection
      zd = new Zendesk.Builder(System.getenv("ZENDESK_URL"))
                   .setUsername(System.getenv("ZENDESK_EMAIL"))
                   .setToken(System.getenv("ZENDESK_TOKEN")).build();

    } catch (Exception ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(1);
    }

    // read the input file.
    Set<String> dxs = new HashSet<>();
    Scanner sc = new Scanner(new File(input));
    while (sc.hasNext()) {
      dxs.add(sc.next());
    }

    Set<String> answer = new TreeSet();
    for (Ticket t : zd.getTickets()) {
      for (CustomFieldValue f : t.getCustomFields()) {
        if (f.getId() == 1260826102050L) {   // custom fields for array of DX numbers.

          if (f.getValue() != null) {
            for (String s : f.getValue()) {
              String[] parts = s.split(",");
              for (String ds : parts) {
                if (dxs.contains(ds)) {
                  Organization org = zd.getOrganization(t.getOrganizationId());
                  String ans = String.format("%s %s %s", org.getName(), t.getId(), ds);
                  answer.add(ans);
                }
              }
            }
          }
        }
      }
    }

    // print alphabetic by org name.
    for (String aa : answer) {
      System.out.println(aa);
    }
  }
}