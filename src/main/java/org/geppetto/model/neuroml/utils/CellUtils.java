
package org.geppetto.model.neuroml.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.neuroml.model.Cell;
import org.neuroml.model.Include;
import org.neuroml.model.Member;
import org.neuroml.model.Point3DWithDiam;
import org.neuroml.model.Segment;
import org.neuroml.model.SegmentGroup;
import org.neuroml.model.util.NeuroMLException;

public class CellUtils
{
	// Get all segments in the cell
	private LinkedHashMap<Integer, Segment> idsVsSegments;
	private Cell cell;

	public CellUtils(Cell cell)
	{
		super();
		this.cell = cell;
		idsVsSegments = getIdsVsSegments();
	}

	public double calculateDistanceInGroup(double distance, Segment segment)
	{
		Point3DWithDiam proximal = (segment.getProximal() == null) ? idsVsSegments.get(segment.getParent().getSegment()).getDistal() : segment.getProximal();
		distance += distance(proximal, segment.getDistal());

		if(segment.getParent() != null)
		{
			return calculateDistanceInGroup(distance, idsVsSegments.get(segment.getParent().getSegment()));
		}
		return distance;
	}

	public LinkedHashMap<Integer, Segment> getIdsVsSegments()
	{

		LinkedHashMap<Integer, Segment> idsVsSegments = new LinkedHashMap<Integer, Segment>();
		for(Segment seg : cell.getMorphology().getSegment())
		{
			idsVsSegments.put(seg.getId(), seg);
		}
		return idsVsSegments;
	}

	private double distance(Point3DWithDiam p, Point3DWithDiam d)
	{
		return Math.sqrt(Math.pow(p.getX() - d.getX(), 2) + Math.pow(p.getY() - d.getY(), 2) + Math.pow(p.getZ() - d.getZ(), 2));
	}

	public List<Segment> getSegmentsInGroup(String segmentGroup) throws NeuroMLException
	{

		for(SegmentGroup sg : cell.getMorphology().getSegmentGroup())
		{
			if(sg.getId().equals(segmentGroup))
			{
				LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups();
				return getSegmentsInGroup(namesVsSegmentGroups, sg);
			}
		}
		throw new NeuroMLException("No SegmentGroup: " + segmentGroup + " in cell with id: " + cell.getId());
	}

	public List<Segment> getSegmentsInGroup(LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups, SegmentGroup segmentGroup) throws NeuroMLException
	{

		List<Segment> segsHere = new ArrayList<Segment>();

		for(Member memb : segmentGroup.getMember())
		{
			segsHere.add(idsVsSegments.get(memb.getSegment()));
		}
		for(Include inc : segmentGroup.getInclude())
		{
			String sg = inc.getSegmentGroup();
			List<Segment> segs = getSegmentsInGroup(namesVsSegmentGroups, namesVsSegmentGroups.get(sg));
			segsHere.addAll(segs);
		}

		return segsHere;
	}

	public LinkedHashMap<SegmentGroup, List<Integer>> getSegmentGroupsVsSegIds()
	{

		LinkedHashMap<SegmentGroup, List<Integer>> sgVsSegId = new LinkedHashMap<SegmentGroup, List<Integer>>();

		LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups();

		for(SegmentGroup sg : cell.getMorphology().getSegmentGroup())
		{
			List<Integer> segsHere = getSegmentIdsInGroup(namesVsSegmentGroups, sg);
			sgVsSegId.put(sg, segsHere);
		}

		return sgVsSegId;
	}
	
	public LinkedHashMap<String, List<Segment>> getSegmentGroupsVsSegs() throws NeuroMLException
	{
		LinkedHashMap<String, List<Segment>> sgVsSegId = new LinkedHashMap<String, List<Segment>>();

		LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups();

		for(SegmentGroup sg : cell.getMorphology().getSegmentGroup())
		{
			List<Segment> segsHere = getSegmentsInGroup(namesVsSegmentGroups, sg);
			sgVsSegId.put(sg.getId(), segsHere);
		}

		return sgVsSegId;
	}

	public LinkedHashMap<String, SegmentGroup> getNamesVsSegmentGroups()
	{

		LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = new LinkedHashMap<String, SegmentGroup>();
		for(SegmentGroup sg : cell.getMorphology().getSegmentGroup())
		{
			namesVsSegmentGroups.put(sg.getId(), sg);
		}
		return namesVsSegmentGroups;
	}

	public List<Integer> getSegmentIdsInGroup(String segmentGroup)
	{

		for(SegmentGroup sg : cell.getMorphology().getSegmentGroup())
		{
			if(sg.getId().equals(segmentGroup))
			{
				LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups();
				return getSegmentIdsInGroup(namesVsSegmentGroups, sg);
			}
		}
		return new ArrayList<Integer>();
	}

	public List<Integer> getSegmentIdsInGroup(SegmentGroup segmentGroup)
	{
		LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups();
		return getSegmentIdsInGroup(namesVsSegmentGroups, segmentGroup);
	}

	public List<Integer> getSegmentIdsInGroup(LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups, SegmentGroup segmentGroup)
	{

		List<Integer> segsHere = new ArrayList<Integer>();

		for(Member memb : segmentGroup.getMember())
		{
			segsHere.add(memb.getSegment());
		}
		for(Include inc : segmentGroup.getInclude())
		{
			String sg = inc.getSegmentGroup();
			List<Integer> segs = getSegmentIdsInGroup(namesVsSegmentGroups, namesVsSegmentGroups.get(sg));
			segsHere.addAll(segs);
		}

		return segsHere;
	}
}
