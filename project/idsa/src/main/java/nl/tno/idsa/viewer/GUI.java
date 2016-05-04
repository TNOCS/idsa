package nl.tno.idsa.viewer;

import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.framework.messaging.ProgressNotifier;
import nl.tno.idsa.framework.population.PopulationGenerator;
import nl.tno.idsa.framework.simulator.Sim;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.*;
import nl.tno.idsa.library.activities.multipliers.Winter;
import nl.tno.idsa.library.locations.PoliceSpawnPoint;
import nl.tno.idsa.library.population.PopulationDataNL;
import nl.tno.idsa.library.world.WorldModelNL;
import nl.tno.idsa.viewer.components.ProgressDialog;

import java.util.List;

// TODO Document class. Mark it as the main class.
public class GUI {

    private static final boolean makeAgendas = true; // TODO This is a setting.

    public static void main(String[] args) throws Exception {

        ProgressDialog progressDialog = new ProgressDialog(null);
        ProgressNotifier.addObserver(progressDialog);

        Messenger.enableMirrorToConsole(true); // TODO Catch console messages in a graphical element.

        ProgressNotifier.notifyShowProgress(true);
        ProgressNotifier.notifyProgressMessage("Loading world data...");

        World world = WorldGenerator.generateWorld(new WorldModelNL(), "../../data/idsa_nav_network_pedestrian.shp", "../../data/idsa_pand_osm_a_utm31n.shp", "../../data/idsa_public_areas_a_utm31n.shp", "../../data/idsa_vbo_utm31n.shp", "../../data/idsa_pand_p_utm31n.shp");

        ProgressNotifier.notifyProgressMessage("Creating environment...");

        // TODO Show a dialog for time and day.
        Environment env;
        // Sunday 11:00 Summer
        // env = new Environment(world, new Summer(), null, new Day(14, 6, 2015), new Time(11, 0, 0));
        // Sunday 11:00 Winter
        // env = new Environment(world, new Winter(), null, new Day(16, 11, 2015), new Time(11, 0, 0));
        // Monday 11:00
        // env = new Environment(world, new Winter(), null, new Day(21, 9, 2015), new Time(11, 0, 0));
        // Monday 17:00
        // env = new Environment(world, new Winter(), null, new Day(21, 9, 2015), new Time(17, 0, 0));
        // Monday 11:00
        // env = new Environment(world, new Winter(), null, new Day(21, 9, 2015), new Time(11, 0, 0));
        // Saturday 11:00
        env = new Environment(world, new Winter(), null, new Day(26, 9, 2015), new Time(12, 0, 0));

        PopulationGenerator populationGenerator = new PopulationGenerator(env, new PopulationDataNL());
        ProgressNotifier.notifyProgress(15);
        env.setPopulation(populationGenerator.generatePopulation("../../data/idsa_cbs_buurten_utm31n.shp")); // TODO Hardcoded input file.
        ProgressNotifier.notifyProgress(85);
        env.initializePopulation(env.getSeason(), null, env.getDay(), env.getTime(), makeAgendas);

        // Add some police stations, randomly, as they are not in the world yet.
        // TODO Create police stations in the world.
        System.out.println("Enriching environment...");
        List<Vertex> vertices = world.getVertices();
        int changedVertices = 0;
        while (changedVertices < 50) {
            Vertex randomVertex = vertices.get(RandomNumber.nextInt(vertices.size()));
            if (randomVertex.getArea() != null) {
                randomVertex.getArea().addFunction(new PoliceSpawnPoint());
                changedVertices++;
            }
        }
        ProgressNotifier.notifyProgress(100);

        // Hide progress
        ProgressNotifier.notifyShowProgress(false);
        ProgressNotifier.removeObserver(progressDialog);

        // Open the viewer
        System.out.println("Creating viewer...");
        MainFrame mf = new MainFrame(env);
        mf.show();

        // Start the sim
        System.out.println("Starting simulator...");
        Sim.getInstance().setXRealTime(30);
        Sim.getInstance().start();
    }
}


